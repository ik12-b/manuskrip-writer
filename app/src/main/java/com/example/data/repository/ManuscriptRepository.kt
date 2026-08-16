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

    suspend fun addNewDocument(
        title: String,
        repository: String,
        datePeriod: String,
        scriptType: String,
        firstFolioLines: List<String>
    ) = withContext(Dispatchers.IO) {
        val docId = "doc_${System.currentTimeMillis()}"
        val folioId = "folio_${docId}_1r"

        val document = DocumentEntity(
            id = docId,
            title = title,
            repository = repository,
            datePeriod = datePeriod,
            language = "Arabic (العربية)",
            scriptType = scriptType,
            totalFolios = 1,
            description = "Manuskrip kustom ditambahkan oleh pengguna."
        )

        val folio = FolioEntity(
            id = folioId,
            documentId = docId,
            folioNumber = "1r",
            title = "Folio 1r",
            totalLines = firstFolioLines.size
        )

        val lines = firstFolioLines.mapIndexed { index, text ->
            LineEntity(
                id = "line_${folioId}_${index + 1}",
                folioId = folioId,
                lineNumber = index + 1,
                originalScriptText = text,
                contextTranslation = "",
                scriptStyle = scriptType,
                bboxTop = 0.08f * (index + 1),
                bboxLeft = 0.05f,
                bboxWidth = 0.90f,
                bboxHeight = 0.07f
            )
        }

        dao.insertDocument(document)
        dao.insertFolios(listOf(folio))
        dao.insertLines(lines)
    }
}
