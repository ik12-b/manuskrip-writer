package com.example.data.repository

import com.example.data.local.ManuscriptDao
import com.example.data.model.DocumentEntity
import com.example.data.model.DocumentWithFolios
import com.example.data.model.FolioEntity
import com.example.data.model.LineEntity
import com.example.data.model.LineWithTranscription
import com.example.data.model.SyncQueueEntity
import com.example.data.model.TranscriptionEntity
import com.example.data.remote.HtrRecognitionResult
import com.example.data.remote.HtrService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONObject

class ManuscriptRepository(
    private val dao: ManuscriptDao,
    private val htrService: HtrService = HtrService()
) {

    val allDocuments: Flow<List<DocumentEntity>> = dao.getAllDocuments()
    val pendingSyncCount: Flow<Int> = dao.getPendingSyncCount()
    val completedCount: Flow<Int> = dao.getCompletedTranscriptionsCount()
    val totalLinesCount: Flow<Int> = dao.getTotalLinesCount()

    fun getDocumentWithFolios(documentId: String): Flow<DocumentWithFolios?> {
        return dao.getDocumentWithFolios(documentId)
    }

    fun getFoliosForDocument(documentId: String): Flow<List<FolioEntity>> {
        return dao.getFoliosForDocument(documentId)
    }

    fun getLinesWithTranscriptions(folioId: String): Flow<List<LineWithTranscription>> {
        return dao.getLinesWithTranscriptions(folioId)
    }

    suspend fun getTranscription(lineId: String): TranscriptionEntity? = withContext(Dispatchers.IO) {
        dao.getTranscription(lineId)
    }

    suspend fun saveTranscription(
        lineId: String,
        text: String,
        status: String = "draft",
        notes: String = "",
        confidence: Float = 0.0f,
        alternativeReadings: String = "",
        annotator: String = "Transcriber"
    ) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val transcription = TranscriptionEntity(
            lineId = lineId,
            text = text,
            status = status,
            notes = notes,
            confidence = confidence,
            alternativeReadings = alternativeReadings,
            annotator = annotator,
            updatedAt = now,
            syncedAt = null // Needs sync
        )
        dao.saveTranscription(transcription)

        // Add to local sync queue (Batch Sync Architecture)
        val payload = JSONObject().apply {
            put("lineId", lineId)
            put("text", text)
            put("status", status)
            put("notes", notes)
            put("updatedAt", now)
        }.toString()

        dao.addToSyncQueue(
            SyncQueueEntity(
                lineId = lineId,
                action = "update",
                payload = payload,
                createdAt = now
            )
        )
    }

    suspend fun recognizeLineHtr(
        line: LineEntity,
        scriptType: String,
        lineImageBase64: String? = null
    ): HtrRecognitionResult = withContext(Dispatchers.IO) {
        htrService.recognizeArabicLine(
            lineId = line.id,
            manuscriptTextPrompt = line.originalScriptText,
            scriptType = scriptType,
            contextInfo = line.contextTranslation,
            lineImageBase64 = lineImageBase64
        )
    }

    suspend fun attachFolioImage(folioId: String, imageUrl: String) = withContext(Dispatchers.IO) {
        dao.updateFolioImage(folioId, imageUrl)
    }

    suspend fun syncPendingBatch(): Int = withContext(Dispatchers.IO) {
        val pendingItems = dao.getPendingSyncQueue(20)
        if (pendingItems.isEmpty()) return@withContext 0

        // Simulate network latency / batch upload
        delay(600)
        val syncedTime = System.currentTimeMillis()
        val idsToDelete = pendingItems.map { it.lineId }

        pendingItems.forEach { item ->
            dao.markTranscriptionSynced(item.lineId, syncedTime)
        }
        dao.deleteFromSyncQueue(idsToDelete)
        return@withContext pendingItems.size
    }

    /**
     * Buat dokumen baru dari FOTO manuskrip asli yang diunggah pengguna, dibagi rata
     * menjadi [lineCount] baris berdasarkan tinggi foto. Belum ada segmentasi baris
     * otomatis (butuh model deteksi baris terpisah) — pembagian rata ini titik awal
     * yang masih bisa berbeda dari baris sesungguhnya di foto.
     *
     * originalScriptText SENGAJA dikosongkan: teksnya memang belum diketahui — itulah
     * yang akan ditranskripsi pengguna dari foto asli (bukan disalin dari data yang
     * sudah ada, seperti pada versi lama aplikasi ini).
     */
    suspend fun addNewDocument(
        title: String,
        repository: String,
        datePeriod: String,
        scriptType: String,
        folioImagePath: String,
        lineCount: Int
    ): String = addNewDocumentFromPages(
        title, repository, datePeriod, scriptType, listOf(folioImagePath), lineCount
    )

    /**
     * Sama seperti [addNewDocument], tapi menerima BANYAK foto folio sekaligus — dipakai
     * saat mengimpor PDF hasil scan multi-halaman (satu halaman PDF = satu folio).
     * [lineCount] diterapkan rata ke setiap halaman.
     */
    suspend fun addNewDocumentFromPages(
        title: String,
        repository: String,
        datePeriod: String,
        scriptType: String,
        folioImagePaths: List<String>,
        lineCount: Int
    ): String = withContext(Dispatchers.IO) {
        val docId = "doc_${System.currentTimeMillis()}"

        val document = DocumentEntity(
            id = docId,
            title = title,
            repository = repository,
            datePeriod = datePeriod,
            language = "Arabic (العربية)",
            scriptType = scriptType,
            totalFolios = folioImagePaths.size,
            description = "Manuskrip diunggah oleh pengguna."
        )

        val folios = mutableListOf<FolioEntity>()
        val allLines = mutableListOf<LineEntity>()
        folioImagePaths.forEachIndexed { pageIndex, imagePath ->
            val folioNumber = "${pageIndex + 1}r"
            val folioId = "folio_${docId}_$folioNumber"
            folios.add(
                FolioEntity(
                    id = folioId,
                    documentId = docId,
                    folioNumber = folioNumber,
                    title = "Folio $folioNumber",
                    imageUrl = imagePath,
                    totalLines = lineCount
                )
            )

            val lineHeight = 1f / lineCount
            (0 until lineCount).forEach { index ->
                allLines.add(
                    LineEntity(
                        id = "line_${folioId}_${index + 1}",
                        folioId = folioId,
                        lineNumber = index + 1,
                        originalScriptText = "",
                        contextTranslation = "",
                        scriptStyle = scriptType,
                        bboxTop = index * lineHeight,
                        bboxLeft = 0.03f,
                        bboxWidth = 0.94f,
                        bboxHeight = lineHeight
                    )
                )
            }
        }

        dao.insertDocument(document)
        dao.insertFolios(folios)
        dao.insertLines(allLines)
        docId
    }

    /**
     * Sisipkan baris kosong baru tepat setelah [afterLineId] pada folio yang sama
     * (atau di akhir kalau [afterLineId] null), lalu bagi ulang bbox SEMUA baris di
     * folio itu secara rata — konsisten dengan cara bbox pertama kali dibuat saat
     * upload, supaya highlight baris di panel foto tetap masuk akal. Ini yang
     * memberi penulis kebebasan mengatur ulang struktur baris, tidak terkunci ke
     * jumlah baris yang dipilih waktu upload.
     */
    suspend fun insertLineAfter(
        folioId: String,
        afterLineId: String?,
        scriptType: String
    ) = withContext(Dispatchers.IO) {
        val existing = dao.getLinesForFolioOnce(folioId).sortedBy { it.lineNumber }
        val insertAt = if (afterLineId == null) {
            existing.size
        } else {
            val idx = existing.indexOfFirst { it.id == afterLineId }
            if (idx == -1) existing.size else idx + 1
        }

        val newLineId = "line_${folioId}_${System.currentTimeMillis()}"
        val newLine = LineEntity(
            id = newLineId,
            folioId = folioId,
            lineNumber = insertAt + 1,
            originalScriptText = "",
            contextTranslation = "",
            scriptStyle = scriptType,
            bboxTop = 0f,
            bboxLeft = 0.03f,
            bboxWidth = 0.94f,
            bboxHeight = 0f // dihitung ulang oleh reflowBboxes() di bawah
        )

        val reflowed = reflowBboxes(existing.toMutableList().apply { add(insertAt, newLine) })
        val newLineFinal = reflowed.first { it.id == newLineId }
        val updatedExisting = reflowed.filter { it.id != newLineId }

        dao.insertLines(listOf(newLineFinal)) // baris BARU: aman pakai insert
        if (updatedExisting.isNotEmpty()) dao.updateLines(updatedExisting) // baris LAMA: UPDATE, bukan insert-replace
    }

    /**
     * Hapus satu baris. Transkripsinya ikut terhapus otomatis lewat cascade delete
     * Room (LineEntity->TranscriptionEntity). Tidak mengizinkan folio sampai kosong
     * tanpa baris sama sekali — return false kalau ini baris terakhir.
     */
    suspend fun deleteLine(folioId: String, lineId: String): Boolean = withContext(Dispatchers.IO) {
        val existing = dao.getLinesForFolioOnce(folioId).sortedBy { it.lineNumber }
        if (existing.size <= 1) return@withContext false

        dao.deleteLine(lineId)
        dao.deleteFromSyncQueue(listOf(lineId))

        val remaining = reflowBboxes(existing.filter { it.id != lineId })
        if (remaining.isNotEmpty()) dao.updateLines(remaining)
        true
    }

    private fun reflowBboxes(lines: List<LineEntity>): List<LineEntity> {
        val lineHeight = 1f / lines.size.coerceAtLeast(1)
        return lines.mapIndexed { index, line ->
            line.copy(
                lineNumber = index + 1,
                bboxTop = index * lineHeight,
                bboxHeight = lineHeight
            )
        }
    }
}
