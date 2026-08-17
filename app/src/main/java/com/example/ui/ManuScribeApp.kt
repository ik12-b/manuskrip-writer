package com.example.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.VerticalAlignCenter
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import android.net.Uri
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddDocumentDialog
import com.example.ui.components.DocumentSelectorDialog
import com.example.ui.components.LineNotesDialog
import com.example.ui.components.ManuscriptHeader
import com.example.ui.components.ManuscriptPdfViewer
import com.example.ui.components.PdfTypewriterSheet
import com.example.ui.components.rememberPdfLinkedSyncController
import com.example.ui.components.TeiExportDialog
import com.example.ui.theme.GoldAmber
import com.example.ui.theme.GoldAmberDark
import com.example.ui.theme.GoldAmberLight
import com.example.ui.theme.IndigoViolet
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ManuScribeApp(
    viewModel: ManuscriptViewModel,
    modifier: Modifier = Modifier
) {
    val documents by viewModel.allDocuments.collectAsStateWithLifecycle()
    val selectedDocument by viewModel.selectedDocument.collectAsStateWithLifecycle()
    val folios by viewModel.folios.collectAsStateWithLifecycle()
    val selectedFolio by viewModel.selectedFolio.collectAsStateWithLifecycle()
    val lines by viewModel.lines.collectAsStateWithLifecycle()
    val currentLineIndex by viewModel.currentLineIndex.collectAsStateWithLifecycle()

    val transcriptionText by viewModel.transcriptionText.collectAsStateWithLifecycle()
    val transcriptionSelection by viewModel.transcriptionSelection.collectAsStateWithLifecycle()
    val transcriptionStatus by viewModel.transcriptionStatus.collectAsStateWithLifecycle()
    val transcriptionNotes by viewModel.transcriptionNotes.collectAsStateWithLifecycle()
    val transcriptionConfidence by viewModel.transcriptionConfidence.collectAsStateWithLifecycle()
    val alternativeReadings by viewModel.alternativeReadings.collectAsStateWithLifecycle()

    val isHtrLoading by viewModel.isHtrLoading.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val pendingSyncCount by viewModel.pendingSyncCount.collectAsStateWithLifecycle()

    val showDocPicker by viewModel.showDocumentPicker.collectAsStateWithLifecycle()
    val showExportDialog by viewModel.showExportDialog.collectAsStateWithLifecycle()
    val showNotesDialog by viewModel.showNotesDialog.collectAsStateWithLifecycle()
    val showAddDocDialog by viewModel.showAddDocDialog.collectAsStateWithLifecycle()
    val uiMessage by viewModel.uiMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Photo Picker untuk melampirkan foto asli folio manuskrip (folio yang sudah ada)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { viewModel.attachFolioImage(it) } }
    val onAttachPhoto: () -> Unit = {
        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    // Photo Picker khusus dialog "Tambah Naskah" (dokumen baru dari foto)
    var newDocImageUri by remember { mutableStateOf<Uri?>(null) }
    val newDocPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { newDocImageUri = it } }

    // Split View Preset State: 0.50f (50/50), 0.65f (Focus Top Manuscript), 0.35f (Focus Bottom Typewriter)
    var splitRatio by remember { mutableFloatStateOf(0.48f) }
    var writingProgressFraction by remember { mutableFloatStateOf(0f) }

    // Linked Synchronized Zoom & Scroll Controller
    val syncController = rememberPdfLinkedSyncController()

    // Animated Split Weight
    val animatedSplitRatio by animateFloatAsState(
        targetValue = splitRatio,
        animationSpec = tween(durationMillis = 250),
        label = "split_ratio"
    )

    // Display UI messages
    LaunchedEffect(uiMessage) {
        uiMessage?.let {
            snackbarHostState.showSnackbar(it.message)
            viewModel.dismissUiMessage()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("manuscribe_main_scaffold"),
        containerColor = ObsidianBg,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Bar
            ManuscriptHeader(
                document = selectedDocument,
                folio = selectedFolio,
                totalLines = lines.size,
                currentLineNumber = (currentLineIndex + 1).coerceAtMost(lines.size.coerceAtLeast(1)),
                pendingSyncCount = pendingSyncCount,
                isSyncing = isSyncing,
                onOpenDocPicker = { viewModel.setShowDocumentPicker(true) },
                onOpenExport = { viewModel.setShowExportDialog(true) },
                onExportPdf = { viewModel.setShowExportDialog(true) },
                onOpenAddDoc = { viewModel.setShowAddDocDialog(true) },
                onSyncNow = { viewModel.syncNow() }
            )

            // Responsive Split-View Container (Portrait vs Landscape/Tablet)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                val isWideScreen = maxWidth > 600.dp && maxWidth > maxHeight

                if (isWideScreen) {
                    // Landscape / Expanded Screen: Side-by-Side Split View
                    Row(modifier = Modifier.fillMaxSize()) {
                        // Left Pane: Manuscript PDF Viewer
                        Box(
                            modifier = Modifier
                                .weight(animatedSplitRatio)
                                .fillMaxHeight()
                        ) {
                            ManuscriptPdfViewer(
                                document = selectedDocument,
                                folio = selectedFolio,
                                lines = lines,
                                currentLineIndex = currentLineIndex,
                                syncController = syncController,
                                writingProgressFraction = writingProgressFraction,
                                onLineSelected = { idx -> viewModel.selectLine(idx) },
                                onAttachPhoto = onAttachPhoto
                            )
                        }

                        // Vertical Split Divider & Ratio Controls
                        VerticalSplitDivider(
                            splitRatio = splitRatio,
                            onSetRatio = { splitRatio = it }
                        )

                        // Right Pane: PDF Typewriter Sheet (Menulis langsung di PDF seperti mesin ketik)
                        Box(
                            modifier = Modifier
                                .weight(1f - animatedSplitRatio)
                                .fillMaxHeight()
                        ) {
                            PdfTypewriterSheet(
                                document = selectedDocument,
                                folio = selectedFolio,
                                lines = lines,
                                currentLineIndex = currentLineIndex,
                                text = transcriptionText,
                                selection = transcriptionSelection,
                                status = transcriptionStatus,
                                notes = transcriptionNotes,
                                confidence = transcriptionConfidence,
                                alternativeReadings = alternativeReadings,
                                isHtrLoading = isHtrLoading,
                                syncController = syncController,
                                onTextChanged = { text -> viewModel.onTranscriptionTextChanged(text) },
                                onSelectionChanged = { sel -> viewModel.onTranscriptionSelectionChanged(sel) },
                                onProgressFractionChanged = { writingProgressFraction = it },
                                onLineSelected = { idx -> viewModel.selectLine(idx) },
                                onNextLine = { viewModel.nextLine() },
                                onPreviousLine = { viewModel.previousLine() },
                                onStatusChanged = { status -> viewModel.setStatus(status) },
                                onOpenNotes = { viewModel.setShowNotesDialog(true) },
                                onRunHtr = { viewModel.runHtrRecognition() },
                                onApplyAlternative = { alt -> viewModel.applyAlternativeReading(alt) },
                                onInsertChar = { char -> viewModel.insertSpecialChar(char) }
                            )
                        }
                    }
                } else {
                    // Portrait / Handheld Screen: Stacked Top & Bottom Split View
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Top Pane: Manuscript PDF Viewer (Gambar Manuskrip Asli)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(animatedSplitRatio)
                        ) {
                            ManuscriptPdfViewer(
                                document = selectedDocument,
                                folio = selectedFolio,
                                lines = lines,
                                currentLineIndex = currentLineIndex,
                                syncController = syncController,
                                writingProgressFraction = writingProgressFraction,
                                onLineSelected = { idx -> viewModel.selectLine(idx) },
                                onAttachPhoto = onAttachPhoto
                            )
                        }

                        // Horizontal Split Divider with Quick Layout Presets
                        HorizontalSplitDivider(
                            splitRatio = splitRatio,
                            onSetRatio = { splitRatio = it }
                        )

                        // Bottom Pane: PDF Typewriter Sheet (Menulis langsung di PDF seperti mesin ketik jaman dulu)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f - animatedSplitRatio)
                        ) {
                            PdfTypewriterSheet(
                                document = selectedDocument,
                                folio = selectedFolio,
                                lines = lines,
                                currentLineIndex = currentLineIndex,
                                text = transcriptionText,
                                selection = transcriptionSelection,
                                status = transcriptionStatus,
                                notes = transcriptionNotes,
                                confidence = transcriptionConfidence,
                                alternativeReadings = alternativeReadings,
                                isHtrLoading = isHtrLoading,
                                syncController = syncController,
                                onTextChanged = { text -> viewModel.onTranscriptionTextChanged(text) },
                                onSelectionChanged = { sel -> viewModel.onTranscriptionSelectionChanged(sel) },
                                onProgressFractionChanged = { writingProgressFraction = it },
                                onLineSelected = { idx -> viewModel.selectLine(idx) },
                                onNextLine = { viewModel.nextLine() },
                                onPreviousLine = { viewModel.previousLine() },
                                onStatusChanged = { status -> viewModel.setStatus(status) },
                                onOpenNotes = { viewModel.setShowNotesDialog(true) },
                                onRunHtr = { viewModel.runHtrRecognition() },
                                onApplyAlternative = { alt -> viewModel.applyAlternativeReading(alt) },
                                onInsertChar = { char -> viewModel.insertSpecialChar(char) }
                            )
                        }
                    }
                }
            }

            // Empty state — belum ada manuskrip sama sekali (database kosong,
            // tidak lagi diisi data contoh otomatis). Overlay di atas viewer
            // kosong, mengarahkan pengguna langsung ke alur unggah foto.
            if (selectedDocument == null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ObsidianBg)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        tint = GoldAmber,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Belum Ada Manuskrip",
                        style = MaterialTheme.typography.titleLarge.copy(color = TextPrimary)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tambahkan naskah dengan mengunggah foto folio manuskrip asli untuk mulai ditranskripsi.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { viewModel.setShowAddDocDialog(true) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldAmber,
                            contentColor = ObsidianBg
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tambah Naskah dari Foto", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Dialogs
        if (showDocPicker) {
            DocumentSelectorDialog(
                documents = documents,
                selectedDocument = selectedDocument,
                folios = folios,
                selectedFolio = selectedFolio,
                onSelectDocument = { doc -> viewModel.selectDocument(doc) },
                onSelectFolio = { folio -> viewModel.selectFolio(folio) },
                onDismiss = { viewModel.setShowDocumentPicker(false) }
            )
        }

        if (showExportDialog) {
            TeiExportDialog(
                document = selectedDocument,
                folio = selectedFolio,
                lines = lines,
                teiXmlContent = viewModel.getTeiXmlExport(),
                markdownContent = viewModel.getMarkdownExport(),
                onDismiss = { viewModel.setShowExportDialog(false) }
            )
        }

        if (showNotesDialog) {
            LineNotesDialog(
                lineNumber = currentLineIndex + 1,
                initialNotes = transcriptionNotes,
                onSaveNotes = { notes ->
                    viewModel.updateNotes(notes)
                    viewModel.setStatus("annotated")
                },
                onDismiss = { viewModel.setShowNotesDialog(false) }
            )
        }

        if (showAddDocDialog) {
            AddDocumentDialog(
                pickedImageUri = newDocImageUri,
                onPickImage = {
                    newDocPhotoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onAddDocument = { title, repo, period, scriptType, lineCount ->
                    viewModel.createNewDocument(title, repo, period, scriptType, lineCount, newDocImageUri)
                    newDocImageUri = null
                },
                onDismiss = {
                    newDocImageUri = null
                    viewModel.setShowAddDocDialog(false)
                }
            )
        }
    }
}

@Composable
private fun HorizontalSplitDivider(
    splitRatio: Float,
    onSetRatio: (Float) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(26.dp),
        color = Color(0xFF181512),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF382E22))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Label
            Text(
                text = "SPLIT VIEW (ATAS: PDF MANUSKRIP • BAWAH: MESIN KETIK PDF)",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF9E8A72),
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            )

            // Preset Buttons: 70/30 (Focus Manuskrip), 50/50 (Seimbang), 30/70 (Focus Mesin Ketik)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SplitPresetChip(
                    label = "📜 Manuskrip",
                    isSelected = splitRatio > 0.55f,
                    onClick = { onSetRatio(0.65f) }
                )

                SplitPresetChip(
                    label = "🌗 50:50",
                    isSelected = splitRatio in 0.45f..0.55f,
                    onClick = { onSetRatio(0.48f) }
                )

                SplitPresetChip(
                    label = "⌨️ Mesin Ketik",
                    isSelected = splitRatio < 0.40f,
                    onClick = { onSetRatio(0.32f) }
                )
            }
        }
    }
}

@Composable
private fun VerticalSplitDivider(
    splitRatio: Float,
    onSetRatio: (Float) -> Unit
) {
    Surface(
        modifier = Modifier
            .width(28.dp)
            .fillMaxHeight(),
        color = Color(0xFF181512),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF382E22))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (splitRatio > 0.55f) GoldAmber else Color(0xFF4A3E2D))
                    .clickable { onSetRatio(0.65f) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (splitRatio in 0.45f..0.55f) GoldAmber else Color(0xFF4A3E2D))
                    .clickable { onSetRatio(0.50f) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (splitRatio < 0.40f) GoldAmber else Color(0xFF4A3E2D))
                    .clickable { onSetRatio(0.35f) }
            )
        }
    }
}

@Composable
private fun SplitPresetChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .clickable { onClick() },
        color = if (isSelected) GoldAmberDark else Color(0xFF2C241B),
        shape = RoundedCornerShape(3.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) GoldAmber else Color(0xFF4A3E2D)
        )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = if (isSelected) Color.White else GoldAmberLight,
                fontSize = 9.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            ),
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
        )
    }
}

