package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.ui.text.TextRange
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ManuscriptDatabase
import com.example.data.model.DocumentEntity
import com.example.data.model.FolioEntity
import com.example.data.model.LineEntity
import com.example.data.model.LineWithTranscription
import com.example.data.repository.ManuscriptRepository
import com.example.utils.BitmapUtils
import com.example.utils.PdfImportUtils
import com.example.utils.TeiXmlExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class UiMessage(
    val id: Long = System.currentTimeMillis(),
    val message: String,
    val isError: Boolean = false
)

class ManuscriptViewModel(application: Application) : AndroidViewModel(application) {

    private val database = ManuscriptDatabase.getInstance(application)
    private val repository = ManuscriptRepository(database.manuscriptDao())

    val allDocuments: StateFlow<List<DocumentEntity>> = repository.allDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingSyncCount: StateFlow<Int> = repository.pendingSyncCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val completedCount: StateFlow<Int> = repository.completedCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalLinesCount: StateFlow<Int> = repository.totalLinesCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _selectedDocument = MutableStateFlow<DocumentEntity?>(null)
    val selectedDocument: StateFlow<DocumentEntity?> = _selectedDocument.asStateFlow()

    private val _folios = MutableStateFlow<List<FolioEntity>>(emptyList())
    val folios: StateFlow<List<FolioEntity>> = _folios.asStateFlow()

    private val _selectedFolio = MutableStateFlow<FolioEntity?>(null)
    val selectedFolio: StateFlow<FolioEntity?> = _selectedFolio.asStateFlow()

    private val _lines = MutableStateFlow<List<LineWithTranscription>>(emptyList())
    val lines: StateFlow<List<LineWithTranscription>> = _lines.asStateFlow()

    private val _currentLineIndex = MutableStateFlow(0)
    val currentLineIndex: StateFlow<Int> = _currentLineIndex.asStateFlow()

    // Active Line Transcription State
    private val _transcriptionText = MutableStateFlow("")
    val transcriptionText: StateFlow<String> = _transcriptionText.asStateFlow()

    // Posisi kursor/seleksi pada teks transkripsi — dipakai supaya insertSpecialChar()
    // (tashkil/hamzah) menyisipkan karakter TEPAT di posisi kursor, bukan selalu di akhir.
    private val _transcriptionSelection = MutableStateFlow(TextRange.Zero)
    val transcriptionSelection: StateFlow<TextRange> = _transcriptionSelection.asStateFlow()

    private val _transcriptionStatus = MutableStateFlow("draft")
    val transcriptionStatus: StateFlow<String> = _transcriptionStatus.asStateFlow()

    private val _transcriptionNotes = MutableStateFlow("")
    val transcriptionNotes: StateFlow<String> = _transcriptionNotes.asStateFlow()

    private val _transcriptionConfidence = MutableStateFlow(0.0f)
    val transcriptionConfidence: StateFlow<Float> = _transcriptionConfidence.asStateFlow()

    private val _alternativeReadings = MutableStateFlow<List<String>>(emptyList())
    val alternativeReadings: StateFlow<List<String>> = _alternativeReadings.asStateFlow()

    // Mode & Interaction State
    private val _isWriteMode = MutableStateFlow(false)
    val isWriteMode: StateFlow<Boolean> = _isWriteMode.asStateFlow()

    private val _isHtrLoading = MutableStateFlow(false)
    val isHtrLoading: StateFlow<Boolean> = _isHtrLoading.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _uiMessage = MutableStateFlow<UiMessage?>(null)
    val uiMessage: StateFlow<UiMessage?> = _uiMessage.asStateFlow()

    // Dialog flags
    private val _showDocumentPicker = MutableStateFlow(false)
    val showDocumentPicker: StateFlow<Boolean> = _showDocumentPicker.asStateFlow()

    private val _showExportDialog = MutableStateFlow(false)
    val showExportDialog: StateFlow<Boolean> = _showExportDialog.asStateFlow()

    private val _showNotesDialog = MutableStateFlow(false)
    val showNotesDialog: StateFlow<Boolean> = _showNotesDialog.asStateFlow()

    private val _showAddDocDialog = MutableStateFlow(false)
    val showAddDocDialog: StateFlow<Boolean> = _showAddDocDialog.asStateFlow()

    private var autoSaveJob: Job? = null
    private var linesCollectJob: Job? = null
    private var foliosCollectJob: Job? = null

    // Dipakai splash screen (lihat MainActivity) supaya splash tetap tampil sampai
    // query pertama ke database benar-benar selesai — bukan cuma splash kosmetik
    // yang hilang di waktu tetap tanpa peduli data sudah siap atau belum.
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    init {
        // repository.allDocuments (Flow Room mentah, BUKAN allDocuments StateFlow di
        // atas) sengaja dipakai di sini — StateFlow hasil .stateIn() sudah punya nilai
        // awal (emptyList()) SEJAK SAAT DIBUAT, jadi .first() di situ akan langsung
        // selesai tanpa benar-benar menunggu query database yang sesungguhnya.
        viewModelScope.launch {
            repository.allDocuments.first()
            _isReady.value = true
        }

        // Automatically select the first document when documents load
        viewModelScope.launch {
            allDocuments.collect { docs ->
                if (_selectedDocument.value == null && docs.isNotEmpty()) {
                    selectDocument(docs.first())
                }
            }
        }
    }

    fun selectDocument(document: DocumentEntity) {
        _selectedDocument.value = document
        foliosCollectJob?.cancel()
        foliosCollectJob = viewModelScope.launch {
            repository.getFoliosForDocument(document.id).collect { folioList ->
                _folios.value = folioList
                if (folioList.isNotEmpty()) {
                    val currentFolioId = _selectedFolio.value?.id
                    val matched = folioList.firstOrNull { it.id == currentFolioId } ?: folioList.first()
                    if (matched.id != currentFolioId) {
                        selectFolio(matched)
                    } else {
                        // Folio sama, cuma datanya diperbarui (mis. foto baru dilampirkan) —
                        // jangan reset baris aktif yang sedang dikerjakan user.
                        _selectedFolio.value = matched
                    }
                }
            }
        }
    }

    fun selectFolio(folio: FolioEntity) {
        _selectedFolio.value = folio
        _currentLineIndex.value = 0
        linesCollectJob?.cancel()
        linesCollectJob = viewModelScope.launch {
            repository.getLinesWithTranscriptions(folio.id).collect { lineList ->
                _lines.value = lineList
                if (lineList.isNotEmpty()) {
                    val validIndex = _currentLineIndex.value.coerceIn(0, lineList.size - 1)
                    updateActiveLineState(lineList[validIndex])
                }
            }
        }
    }

    fun selectLine(index: Int) {
        val lineList = _lines.value
        if (index in lineList.indices) {
            // Save existing line before switching if dirty
            saveCurrentLineImmediately()
            _currentLineIndex.value = index
            updateActiveLineState(lineList[index])
        }
    }

    /**
     * Sisipkan baris kosong baru tepat setelah baris yang sedang aktif, lalu pindah
     * ke baris baru itu. Inilah yang memberi penulis kebebasan mengatur struktur —
     * tidak terkunci ke jumlah baris yang dipilih waktu upload foto/PDF.
     */
    fun addLineAfterCurrent() {
        val folio = _selectedFolio.value ?: return
        val doc = _selectedDocument.value ?: return
        val currentLines = _lines.value
        val idx = _currentLineIndex.value
        val afterLineId = currentLines.getOrNull(idx)?.line?.id

        viewModelScope.launch {
            saveCurrentLineImmediately()
            repository.insertLineAfter(folio.id, afterLineId, doc.scriptType)
            // Daftar baris akan otomatis refresh lewat Flow Room (linesCollectJob) —
            // set target index duluan supaya begitu daftar baru datang, langsung
            // pindah & fokus ke baris kosong yang baru dibuat.
            _currentLineIndex.value = idx + 1
            showToast("Baris baru ditambahkan.")
        }
    }

    /**
     * Hapus baris yang sedang aktif. Minimal harus ada 1 baris tersisa di folio.
     */
    fun deleteCurrentLine() {
        val folio = _selectedFolio.value ?: return
        val currentLines = _lines.value
        val idx = _currentLineIndex.value
        val line = currentLines.getOrNull(idx)?.line ?: return

        viewModelScope.launch {
            val deleted = repository.deleteLine(folio.id, line.id)
            if (deleted) {
                showToast("Baris dihapus.")
            } else {
                showToast("Tidak bisa menghapus — minimal harus ada 1 baris.", isError = true)
            }
        }
    }

    fun nextLine() {
        val nextIdx = _currentLineIndex.value + 1
        if (nextIdx < _lines.value.size) {
            selectLine(nextIdx)
        }
    }

    fun previousLine() {
        val prevIdx = _currentLineIndex.value - 1
        if (prevIdx >= 0) {
            selectLine(prevIdx)
        }
    }

    private fun updateActiveLineState(item: LineWithTranscription) {
        val trans = item.transcription
        _transcriptionText.value = trans?.text ?: item.line.originalScriptText
        _transcriptionSelection.value = TextRange(_transcriptionText.value.length)
        _transcriptionStatus.value = trans?.status ?: "draft"
        _transcriptionNotes.value = trans?.notes ?: ""
        _transcriptionConfidence.value = trans?.confidence ?: 0.0f
        _alternativeReadings.value = trans?.alternativeReadings
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() } ?: emptyList()
    }

    fun onTranscriptionTextChanged(newText: String, newSelection: TextRange = TextRange(newText.length)) {
        _transcriptionText.value = newText
        _transcriptionSelection.value = newSelection
        debounceAutoSave()
    }

    fun onTranscriptionSelectionChanged(newSelection: TextRange) {
        _transcriptionSelection.value = newSelection
    }

    fun setStatus(status: String) {
        _transcriptionStatus.value = status
        saveCurrentLineImmediately()
        showToast("Status diubah ke: ${getStatusLabel(status)}")
    }

    fun updateNotes(notes: String) {
        _transcriptionNotes.value = notes
        saveCurrentLineImmediately()
    }

    fun insertSpecialChar(char: String) {
        // Sisipkan TEPAT di posisi kursor terakhir yang diketahui, bukan selalu di akhir teks —
        // penting untuk tashkil/hamzah yang harus presisi menempel pada huruf tertentu.
        val current = _transcriptionText.value
        val selection = _transcriptionSelection.value
        val start = selection.start.coerceIn(0, current.length)
        val end = selection.end.coerceIn(0, current.length)
        val newText = current.substring(0, start) + char + current.substring(end)
        val newCursor = start + char.length

        _transcriptionText.value = newText
        _transcriptionSelection.value = TextRange(newCursor)
        debounceAutoSave()
    }

    fun applyAlternativeReading(altText: String) {
        _transcriptionText.value = altText
        _transcriptionSelection.value = TextRange(altText.length)
        saveCurrentLineImmediately()
        showToast("Menerapkan varian: $altText")
    }

    private fun debounceAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(1500) // 1.5s idle debounce for eMMC 5.1 storage efficiency
            saveCurrentLineImmediately()
        }
    }

    fun saveCurrentLineImmediately() {
        val currentLines = _lines.value
        val idx = _currentLineIndex.value
        if (idx in currentLines.indices) {
            val line = currentLines[idx].line
            viewModelScope.launch {
                repository.saveTranscription(
                    lineId = line.id,
                    text = _transcriptionText.value,
                    status = _transcriptionStatus.value,
                    notes = _transcriptionNotes.value,
                    confidence = _transcriptionConfidence.value,
                    alternativeReadings = _alternativeReadings.value.joinToString(",")
                )
            }
        }
    }

    fun runHtrRecognition() {
        val currentLines = _lines.value
        val idx = _currentLineIndex.value
        if (idx !in currentLines.indices) return

        val line = currentLines[idx].line
        val doc = _selectedDocument.value ?: return
        val folioImagePath = _selectedFolio.value?.imageUrl

        viewModelScope.launch {
            _isHtrLoading.value = true
            try {
                // Kalau folio sudah punya foto asli, potong baris ini dari foto dan kirim
                // sebagai GAMBAR ke HTR — supaya model benar-benar membaca tulisan tangan,
                // bukan menyalin balik teks referensi yang sudah ada di database (bug lama).
                val lineImageBase64 = withContext(Dispatchers.IO) {
                    folioImagePath?.let { path ->
                        BitmapUtils.cropLineFromFolio(path, line)?.let { bmp ->
                            val encoded = BitmapUtils.toBase64Jpeg(bmp)
                            bmp.recycle()
                            encoded
                        }
                    }
                }

                val result = repository.recognizeLineHtr(line, doc.scriptType, lineImageBase64)
                _transcriptionText.value = result.recognizedText
                _transcriptionSelection.value = TextRange(result.recognizedText.length)
                _transcriptionConfidence.value = result.confidence
                _alternativeReadings.value = result.alternativeReadings
                if (result.notes.isNotBlank() && _transcriptionNotes.value.isBlank()) {
                    _transcriptionNotes.value = result.notes
                }
                saveCurrentLineImmediately()

                if (lineImageBase64 == null) {
                    showToast("Belum ada foto folio — hasil ini BUKAN OCR, cuma teks referensi. Lampirkan foto (ikon kamera) untuk HTR sungguhan.", isError = true)
                } else {
                    showToast("HTR Selesai: Presisi ${(result.confidence * 100).toInt()}%")
                }
            } catch (e: Exception) {
                showToast("HTR Gagal: ${e.message}", isError = true)
            } finally {
                _isHtrLoading.value = false
            }
        }
    }

    /**
     * Lampirkan foto folio (dari photo picker/kamera) supaya panel manuskrip menampilkan
     * foto asli, dan HTR bisa membaca dari gambar sungguhan alih-alih teks referensi.
     */
    fun attachFolioImage(uri: Uri) {
        val folio = _selectedFolio.value
        if (folio == null) {
            showToast("Pilih folio dahulu sebelum melampirkan foto.", isError = true)
            return
        }
        viewModelScope.launch {
            val path = withContext(Dispatchers.IO) {
                BitmapUtils.persistPickedImage(getApplication(), uri, folio.id)
            }
            if (path != null) {
                repository.attachFolioImage(folio.id, path)
                showToast("Foto folio berhasil dilampirkan.")
            } else {
                showToast("Gagal menyimpan foto folio.", isError = true)
            }
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                val count = repository.syncPendingBatch()
                if (count > 0) {
                    showToast("Berhasil menyinkronkan $count baris ke queue!")
                } else {
                    showToast("Semua transkripsi sudah sinkron.")
                }
            } catch (e: Exception) {
                showToast("Sinkronisasi gagal: ${e.message}", isError = true)
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun getTeiXmlExport(): String {
        val doc = _selectedDocument.value ?: return ""
        val folio = _selectedFolio.value ?: return ""
        return TeiXmlExporter.exportToTeiP5Xml(doc, folio, _lines.value)
    }

    fun getMarkdownExport(): String {
        val doc = _selectedDocument.value ?: return ""
        val folio = _selectedFolio.value ?: return ""
        return TeiXmlExporter.exportToMarkdown(doc, folio, _lines.value)
    }

    fun exportAlignedManuscriptPdf(context: android.content.Context): java.io.File? {
        val doc = _selectedDocument.value ?: return null
        val folio = _selectedFolio.value ?: return null
        return try {
            val file = com.example.utils.PdfDocumentExporter.generateAlignedManuscriptPdf(context, doc, folio, _lines.value)
            showToast("PDF Berhasil Dibuat: ${file.name}")
            file
        } catch (e: Exception) {
            showToast("Gagal membuat PDF: ${e.localizedMessage}", isError = true)
            null
        }
    }

    fun createNewDocument(
        title: String,
        repositoryName: String,
        datePeriod: String,
        scriptType: String,
        lineCount: Int,
        fileUri: Uri?
    ) {
        if (title.isBlank()) {
            showToast("Judul naskah wajib diisi!", isError = true)
            return
        }
        if (fileUri == null) {
            showToast("Pilih foto atau PDF manuskrip terlebih dahulu!", isError = true)
            return
        }
        if (lineCount <= 0) {
            showToast("Jumlah baris harus lebih dari 0!", isError = true)
            return
        }

        viewModelScope.launch {
            val context: Context = getApplication()
            val isPdf = withContext(Dispatchers.IO) { PdfImportUtils.isPdf(context, fileUri) }

            val docId = if (isPdf) {
                // Import PDF hasil scan — satu halaman PDF menjadi satu folio.
                val pages = withContext(Dispatchers.IO) {
                    PdfImportUtils.importPdfPages(context, fileUri, "newdoc_${System.currentTimeMillis()}")
                }
                if (pages.isNullOrEmpty()) {
                    showToast("Gagal membaca PDF — pastikan file tidak rusak/terkunci.", isError = true)
                    return@launch
                }
                repository.addNewDocumentFromPages(
                    title = title,
                    repository = repositoryName.ifBlank { "Koleksi Pribadi" },
                    datePeriod = datePeriod.ifBlank { "Kontemporer" },
                    scriptType = scriptType.ifBlank { "Naskh" },
                    folioImagePaths = pages.sortedBy { it.pageIndex }.map { it.imagePath },
                    lineCount = lineCount
                )
            } else {
                // Foto tunggal — satu folio.
                val imagePath = withContext(Dispatchers.IO) {
                    BitmapUtils.persistPickedImage(context, fileUri, "newdoc_${System.currentTimeMillis()}")
                }
                if (imagePath == null) {
                    showToast("Gagal menyimpan foto manuskrip.", isError = true)
                    return@launch
                }
                repository.addNewDocument(
                    title = title,
                    repository = repositoryName.ifBlank { "Koleksi Pribadi" },
                    datePeriod = datePeriod.ifBlank { "Kontemporer" },
                    scriptType = scriptType.ifBlank { "Naskh" },
                    folioImagePath = imagePath,
                    lineCount = lineCount
                )
            }

            _showAddDocDialog.value = false
            showToast("Dokumen '$title' dibuat — silakan transkripsi tiap baris.")

            // Langsung pindah ke dokumen yang baru dibuat
            allDocuments.value.firstOrNull { it.id == docId }?.let { selectDocument(it) }
        }
    }

    fun setWriteMode(enabled: Boolean) {
        _isWriteMode.value = enabled
    }

    fun setShowDocumentPicker(show: Boolean) {
        _showDocumentPicker.value = show
    }

    fun setShowExportDialog(show: Boolean) {
        _showExportDialog.value = show
    }

    fun setShowNotesDialog(show: Boolean) {
        _showNotesDialog.value = show
    }

    fun setShowAddDocDialog(show: Boolean) {
        _showAddDocDialog.value = show
    }

    fun dismissUiMessage() {
        _uiMessage.value = null
    }

    private fun showToast(msg: String, isError: Boolean = false) {
        _uiMessage.value = UiMessage(message = msg, isError = isError)
    }

    private fun getStatusLabel(status: String): String = when (status) {
        "completed" -> "Selesai (Completed)"
        "unclear" -> "Tidak Jelas (Unclear)"
        "annotated" -> "Catatan (Annotated)"
        else -> "Draft"
    }
}
