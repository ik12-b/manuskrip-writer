package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DocumentEntity
import com.example.data.model.FolioEntity
import com.example.data.model.LineWithTranscription
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.GoldAmber
import com.example.ui.theme.GoldAmberDark
import com.example.ui.theme.GoldAmberLight
import com.example.ui.theme.IndigoViolet
import com.example.ui.theme.IndigoVioletLight
import com.example.ui.theme.StatusAnnotated
import com.example.ui.theme.StatusCompleted
import com.example.ui.theme.StatusDraft
import com.example.ui.theme.StatusUnclear
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.max

enum class TypewriterRibbon(val label: String, val inkColor: Color, val badgeColor: Color) {
    BLACK("Tinta Hitam", Color(0xFF1E293B), Color(0xFF475569)),
    RUBRIC_RED("Tinta Merah Rubrikasi", Color(0xFFDC2626), Color(0xFFEF4444)),
    GOLD_TADZHIB("Tinta Emas Tadzhib", Color(0xFFD97706), Color(0xFFF59E0B))
}

@Composable
fun PdfTypewriterSheet(
    document: DocumentEntity?,
    folio: FolioEntity?,
    lines: List<LineWithTranscription>,
    currentLineIndex: Int,
    text: String,
    selection: TextRange = TextRange(text.length),
    status: String,
    notes: String,
    confidence: Float,
    alternativeReadings: List<String>,
    isHtrLoading: Boolean,
    syncController: PdfLinkedSyncController,
    onTextChanged: (String) -> Unit,
    onSelectionChanged: (TextRange) -> Unit = {},
    onProgressFractionChanged: (Float) -> Unit = {},
    onLineSelected: (Int) -> Unit,
    onNextLine: () -> Unit,
    onPreviousLine: () -> Unit,
    onStatusChanged: (String) -> Unit,
    onOpenNotes: () -> Unit,
    onRunHtr: () -> Unit,
    onApplyAlternative: (String) -> Unit,
    onInsertChar: (String) -> Unit,
    onAddLine: () -> Unit = {},
    onDeleteLine: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedRibbon by remember { mutableStateOf(TypewriterRibbon.BLACK) }
    var autoCarriageShift by remember { mutableStateOf(true) } // Auto shift like antique typewriter carriage

    val focusRequester = remember { FocusRequester() }
    val density = LocalDensity.current

    // Virtual Large PDF Document Sheet Dimensions
    val pdfSheetWidthDp = 580.dp
    val pdfSheetHeightDp = 860.dp

    // Calculate approximate typing progress fraction (0.0 = right edge, 1.0 = left edge in Arabic RTL)
    val maxCharsPerLine = 45f
    val currentProgressFraction = (text.length / maxCharsPerLine).coerceIn(0f, 1f)

    // Notify top viewer of current writing progress
    LaunchedEffect(currentProgressFraction, currentLineIndex) {
        onProgressFractionChanged(currentProgressFraction)
    }

    // Auto Carriage Shift & Synchronized Edge Scrolling
    // When text grows or reaches near the screen edge, shift carriage and sync with top PDF viewer
    LaunchedEffect(currentLineIndex, currentProgressFraction, autoCarriageShift, syncController.bottomZoomScale, syncController.isLinked) {
        if (autoCarriageShift && lines.isNotEmpty()) {
            val safeIdx = currentLineIndex.coerceIn(0, lines.size - 1)
            val lineItem = lines[safeIdx]

            with(density) {
                val pageHeightPx = pdfSheetHeightDp.toPx()
                val pageWidthPx = pdfSheetWidthDp.toPx()

                // Target line vertical center
                val lineYPercent = lineItem.line.bboxTop + (lineItem.line.bboxHeight / 2f)
                val rawTargetY = -((lineYPercent - 0.45f) * pageHeightPx * syncController.bottomZoomScale)
                val targetPanY = rawTargetY.coerceIn(-pageHeightPx * 0.9f, pageHeightPx * 0.9f)

                // Mechanical Typewriter Horizontal Carriage Shift:
                // When typing Arabic (RTL), text grows right-to-left.
                // Platen shifts the sheet left so typing strike stays in view.
                val horizontalShift = (currentProgressFraction - 0.5f) * (pageWidthPx * 0.55f) * syncController.bottomZoomScale
                val targetPanX = (-horizontalShift).coerceIn(-pageWidthPx * 0.7f, pageWidthPx * 0.7f)

                // Carriage-shift OTOMATIS ini cuma menggeser KERTAS, tidak pernah menyentuh
                // sharedPanX/Y — supaya foto manuskrip asli di panel atas tidak ikut
                // bergeser/keluar layout setiap kali mengetik (bug sebelumnya).
                syncController.triggerEdgeScroll(targetPanX, targetPanY)
            }
        }
    }

    // Focus active line input field
    LaunchedEffect(currentLineIndex) {
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) { }
    }

    // Kertas bergerak karena DUA sumber: (1) sharedPanX/Y dari gestur geser/zoom yang
    // memang sengaja ditautkan dengan panel foto (tombol Link), dan (2) typewriterAutoPanX/Y
    // dari carriage-shift otomatis saat mengetik (khusus kertas, tidak pernah menggeser foto).
    val animatedPanX by animateFloatAsState(
        targetValue = syncController.sharedPanX + syncController.typewriterAutoPanX,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "typewriter_pan_x"
    )
    val animatedPanY by animateFloatAsState(
        targetValue = syncController.sharedPanY + syncController.typewriterAutoPanY,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "typewriter_pan_y"
    )
    val animatedZoom by animateFloatAsState(
        targetValue = syncController.bottomZoomScale,
        animationSpec = tween(durationMillis = 220),
        label = "typewriter_zoom"
    )

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("pdf_typewriter_sheet_panel"),
        color = Color(0xFF1B1815)
    ) {
        // imePadding() SUDAH ditangani di container split-view luar (ManuScribeApp),
        // bukan di sini lagi — supaya weight() panel dihitung ulang berdasarkan
        // tinggi yang sudah dikurangi keyboard (bukan cuma padding di dalam kotak
        // yang sudah kadung dialokasikan penuh). Kalau imePadding() dipasang di dua
        // tempat, tinggi bisa terpotong dua kali.
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Mechanical Typewriter Top Roller & Platen Controls
            TypewriterMechanicalHeader(
                currentLineIndex = currentLineIndex,
                totalLines = lines.size.coerceAtLeast(1),
                selectedRibbon = selectedRibbon,
                status = status,
                confidence = confidence,
                isHtrLoading = isHtrLoading,
                zoomScale = syncController.bottomZoomScale,
                isLinked = syncController.isLinked,
                autoCarriageShift = autoCarriageShift,
                onZoomIn = { syncController.onPinchZoom("bottom", 1.2f) },
                onZoomOut = { syncController.onPinchZoom("bottom", 0.83f) },
                onResetZoom = { syncController.resetZoomAndPan() },
                onAutoAlign = { syncController.autoAlignAndCenter() },
                onToggleLink = { syncController.toggleLink() },
                onToggleAutoCarriage = { autoCarriageShift = !autoCarriageShift },
                onRibbonChanged = { selectedRibbon = it },
                onRunHtr = onRunHtr,
                onCarriageReturn = onNextLine,
                onPreviousLine = onPreviousLine,
                onOpenNotes = onOpenNotes,
                onStatusChanged = onStatusChanged,
                onAddLine = onAddLine,
                onDeleteLine = onDeleteLine
            )

            // PDF Document Canvas (Direct In-Place Typewriter Paper with Linked Pinch-to-Zoom)
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF110F0D))
                    .clip(RoundedCornerShape(0.dp))
                    .pointerInput(syncController.isLinked) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            // Linked or independent pinch-to-zoom & synchronized pan with boundaries
                            val containerW = size.width.toFloat()
                            val containerH = size.height.toFloat()
                            val sheetW = with(density) { pdfSheetWidthDp.toPx() }
                            val sheetH = with(density) { pdfSheetHeightDp.toPx() }

                            syncController.onPinchZoom("bottom", zoom)
                            syncController.onPanScroll(
                                source = "bottom",
                                deltaX = pan.x,
                                deltaY = pan.y,
                                containerWidth = containerW,
                                containerHeight = containerH,
                                contentWidth = sheetW,
                                contentHeight = sheetH
                            )
                            
                            if (pan.getDistance() > 12f) {
                                autoCarriageShift = false
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                // Kertas PDF polos — putih standar seperti aplikasi PDF viewer biasa,
                // bukan lagi "parchment ivory" bergaya manuskrip antik.
                Box(
                    modifier = Modifier
                        .size(pdfSheetWidthDp, pdfSheetHeightDp)
                        .graphicsLayer {
                            scaleX = animatedZoom
                            scaleY = animatedZoom
                            translationX = animatedPanX
                            translationY = animatedPanY
                        }
                        .shadow(8.dp, RoundedCornerShape(2.dp))
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFDDDDDD), RoundedCornerShape(2.dp))
                ) {
                    // Content on PDF Page
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp, vertical = 24.dp)
                    ) {
                        // Judul halaman PDF — polos, cuma info folio
                        PdfPageHeader(
                            docTitle = document?.title ?: "Dokumen",
                            folioCode = folio?.folioNumber ?: "1"
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Render each line directly on the PDF document
                        lines.forEachIndexed { index, item ->
                            val isActive = index == currentLineIndex
                            TypewriterDocumentLineItem(
                                item = item,
                                index = index,
                                isActive = isActive,
                                activeText = if (isActive) text else (item.transcription?.text ?: ""),
                                activeSelection = if (isActive) selection else TextRange.Zero,
                                selectedRibbon = selectedRibbon,
                                focusRequester = if (isActive) focusRequester else null,
                                onTextChanged = { newText ->
                                    if (isActive) {
                                        autoCarriageShift = true
                                        onTextChanged(newText)
                                    }
                                },
                                onSelectionChanged = { newSelection ->
                                    if (isActive) {
                                        onSelectionChanged(newSelection)
                                    }
                                },
                                onSelect = {
                                    autoCarriageShift = true
                                    onLineSelected(index)
                                },
                                onCarriageReturn = onNextLine
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Nomor halaman — polos seperti footer PDF viewer biasa
                        PdfPageFooter(
                            folioCode = folio?.folioNumber ?: "1",
                            totalLines = lines.size
                        )
                    }
                }

                // (indikator crosshair mekanis khas mesin ketik sudah dihapus —
                // tidak relevan untuk tampilan kertas PDF polos)

                // Synchronized Edge Scroll & Link Badge
                if (syncController.isLinked) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xDD0F172A),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = "Linked",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Zoom & Scroll Tertaut",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFFE0F2FE),
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            // Alternative HTR Suggestions Bar (If Available)
            if (alternativeReadings.isNotEmpty()) {
                AlternativeReadingsBar(
                    alternatives = alternativeReadings,
                    onApply = onApplyAlternative
                )
            }
        }
    }
}

@Composable
private fun TypewriterMechanicalHeader(
    currentLineIndex: Int,
    totalLines: Int,
    selectedRibbon: TypewriterRibbon,
    status: String,
    confidence: Float,
    isHtrLoading: Boolean,
    zoomScale: Float,
    isLinked: Boolean,
    autoCarriageShift: Boolean,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onResetZoom: () -> Unit,
    onAutoAlign: () -> Unit,
    onToggleLink: () -> Unit,
    onToggleAutoCarriage: () -> Unit,
    onRibbonChanged: (TypewriterRibbon) -> Unit,
    onRunHtr: () -> Unit,
    onCarriageReturn: () -> Unit,
    onPreviousLine: () -> Unit,
    onOpenNotes: () -> Unit,
    onStatusChanged: (String) -> Unit,
    onAddLine: () -> Unit,
    onDeleteLine: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp),
        color = Color(0xFF24201C),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4A3E2D))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 5.dp)) {
            // Top Row: Mechanical Typewriter Brand & Roller Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Typewriter Brand Badge & Line Stepper Indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(GoldAmber)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "MESIN KETIK PDF • BARIS ${currentLineIndex + 1}/$totalLines",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = GoldAmberLight,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            fontSize = 10.sp
                        )
                    )
                }

                // Linked Sync, Auto Carriage Shift Toggle & Ribbon Color Selector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    // Linked Sync Toggle Chip
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onToggleLink() },
                        color = if (isLinked) Color(0xFF0C4A6E) else Color(0xFF332B22),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isLinked) Color(0xFF38BDF8) else Color(0xFF5A4935)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isLinked) Icons.Default.Link else Icons.Default.LinkOff,
                                contentDescription = "Tautkan Sync",
                                tint = if (isLinked) Color(0xFF7DD3FC) else TextMuted,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = if (isLinked) "Taut" else "Lepas",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isLinked) Color(0xFFBAE6FD) else TextMuted,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    // Auto Carriage Mode Chip
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onToggleAutoCarriage() },
                        color = if (autoCarriageShift) IndigoViolet.copy(alpha = 0.3f) else Color(0xFF332B22),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (autoCarriageShift) IndigoViolet else Color(0xFF5A4935)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (autoCarriageShift) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = "Auto Geser Mesin Ketik",
                                tint = if (autoCarriageShift) IndigoVioletLight else TextMuted,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = if (autoCarriageShift) "Geser" else "Bebas",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (autoCarriageShift) IndigoVioletLight else TextMuted,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    // Ribbon Color Pickers
                    TypewriterRibbon.values().forEach { ribbon ->
                        val isSelected = ribbon == selectedRibbon
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(ribbon.badgeColor)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { onRibbonChanged(ribbon) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Action Controls Row: Roller Stepper, Carriage Return, Zoom & AI Assist
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Roller Line Stepper & Carriage Return Lever
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onPreviousLine,
                        enabled = currentLineIndex > 0,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Roller Atas",
                            tint = if (currentLineIndex > 0) GoldAmberLight else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onCarriageReturn,
                        enabled = currentLineIndex < totalLines - 1,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Roller Bawah",
                            tint = if (currentLineIndex < totalLines - 1) GoldAmberLight else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Carriage Return Lever Button
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onCarriageReturn() },
                        color = Color(0xFF382F24),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldAmberDark)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardReturn,
                                contentDescription = "Carriage Return",
                                tint = GoldAmber,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Roller ⏎",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = GoldAmberLight,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }

                // Zoom Level Controls for PDF Sheet (Pinch & Button synced)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onZoomOut,
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Zoom Out",
                            tint = TextSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    Text(
                        text = "${(zoomScale * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextPrimary,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.width(30.dp),
                        textAlign = TextAlign.Center
                    )

                    IconButton(
                        onClick = onZoomIn,
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Zoom In",
                            tint = TextSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    IconButton(
                        onClick = onResetZoom,
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset Zoom",
                            tint = GoldAmber,
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    // Auto-Align & Center PDF Sheet Button
                    IconButton(
                        onClick = onAutoAlign,
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CenterFocusStrong,
                            contentDescription = "Auto-Align PDF",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                // Right Actions: Status Tag & AI Auto HTR
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                // Tambah Baris — sisipkan baris kosong setelah baris aktif, supaya
                // penulis tidak terkunci ke jumlah baris yang dipilih waktu upload.
                IconButton(
                    onClick = onAddLine,
                    modifier = Modifier
                        .size(24.dp)
                        .testTag("typewriter_add_line_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Tambah Baris",
                        tint = Color(0xFF4ADE80),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Hapus Baris Aktif
                IconButton(
                    onClick = onDeleteLine,
                    modifier = Modifier
                        .size(24.dp)
                        .testTag("typewriter_delete_line_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Hapus Baris",
                        tint = Color(0xFFF87171),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Notes Button
                IconButton(
                    onClick = onOpenNotes,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = "Catatan Pinggir",
                        tint = if (status == "annotated") StatusAnnotated else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                    // Status Toggle Dropdown Chip
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                val nextStatus = when (status) {
                                    "draft" -> "completed"
                                    "completed" -> "unclear"
                                    "unclear" -> "annotated"
                                    else -> "draft"
                                }
                                onStatusChanged(nextStatus)
                            },
                        color = when (status) {
                            "completed" -> StatusCompleted.copy(alpha = 0.2f)
                            "unclear" -> StatusUnclear.copy(alpha = 0.2f)
                            "annotated" -> StatusAnnotated.copy(alpha = 0.2f)
                            else -> StatusDraft.copy(alpha = 0.2f)
                        },
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            when (status) {
                                "completed" -> StatusCompleted
                                "unclear" -> StatusUnclear
                                "annotated" -> StatusAnnotated
                                else -> StatusDraft
                            }
                        )
                    ) {
                        Text(
                            text = when (status) {
                                "completed" -> "✓ Sah"
                                "unclear" -> "? Ragu"
                                "annotated" -> "★ Catatan"
                                else -> "✎ Draft"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = when (status) {
                                    "completed" -> StatusCompleted
                                    "unclear" -> StatusUnclear
                                    "annotated" -> StatusAnnotated
                                    else -> StatusDraft
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }

                    // AI Auto-Type Assistant Button
                    FilledTonalButton(
                        onClick = onRunHtr,
                        enabled = !isHtrLoading,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0x336366F1),
                            contentColor = IndigoVioletLight
                        ),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 1.dp),
                        modifier = Modifier
                            .height(24.dp)
                            .testTag("typewriter_auto_htr_btn")
                    ) {
                        if (isHtrLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(10.dp),
                                color = IndigoVioletLight,
                                strokeWidth = 1.5.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Auto HTR",
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Auto",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PdfPageHeader(
    docTitle: String,
    folioCode: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 12.dp)
    ) {
        Text(
            text = "$docTitle — Folio $folioCode",
            style = MaterialTheme.typography.labelMedium.copy(
                color = Color(0xFF333333),
                fontSize = 11.sp
            )
        )
        HorizontalDivider(
            modifier = Modifier.padding(top = 4.dp),
            thickness = 1.dp,
            color = Color(0xFFE5E5E5)
        )
    }
}

@Composable
private fun TypewriterDocumentLineItem(
    item: LineWithTranscription,
    index: Int,
    isActive: Boolean,
    activeText: String,
    activeSelection: TextRange,
    selectedRibbon: TypewriterRibbon,
    focusRequester: FocusRequester?,
    onTextChanged: (String) -> Unit,
    onSelectionChanged: (TextRange) -> Unit,
    onSelect: () -> Unit,
    onCarriageReturn: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cursor_blink")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor_alpha"
    )

    val activeLineBg = if (isActive) Color(0xFFFFF7DD) else Color.Transparent
    val activeLineBorder = if (isActive) GoldAmberDark else Color.Transparent

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(activeLineBg)
            .border(
                width = if (isActive) 1.5.dp else 0.dp,
                color = activeLineBorder,
                shape = RoundedCornerShape(3.dp)
            )
            .clickable { onSelect() }
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .testTag("typewriter_doc_line_${index + 1}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Line Number / Platen index
            Row(
                modifier = Modifier.width(42.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isActive) {
                    Text(
                        text = "▶",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = GoldAmberDark,
                            fontSize = 8.sp
                        ),
                        modifier = Modifier.padding(end = 2.dp)
                    )
                }

                Text(
                    text = "${index + 1}.",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isActive) Color(0xFF92400E) else Color(0xFFA89F91),
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                )
            }

            // Arabic Typewritten Text Area on the PDF Page
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp, end = 4.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    if (isActive) {
                        // Direct In-Place Typewriter Input Field
                        var modifierField = Modifier
                            .fillMaxWidth()
                            .testTag("typewriter_active_input")

                        if (focusRequester != null) {
                            modifierField = modifierField.focusRequester(focusRequester)
                        }

                        val fieldValue = TextFieldValue(
                            text = activeText,
                            selection = TextRange(
                                activeSelection.start.coerceIn(0, activeText.length),
                                activeSelection.end.coerceIn(0, activeText.length)
                            )
                        )

                        BasicTextField(
                            value = fieldValue,
                            onValueChange = { newValue ->
                                onTextChanged(newValue.text)
                                onSelectionChanged(newValue.selection)
                            },
                            modifier = modifierField,
                            textStyle = TextStyle(
                                fontSize = 20.sp,
                                fontFamily = AmiriFontFamily,
                                color = selectedRibbon.inkColor,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Right,
                                textDirection = TextDirection.Rtl,
                                lineHeight = 28.sp
                            ),
                            cursorBrush = SolidColor(selectedRibbon.inkColor.copy(alpha = cursorAlpha)),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next,
                                autoCorrect = false
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { onCarriageReturn() }
                            ),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    if (activeText.isEmpty()) {
                                        Text(
                                            text = "Ketik langsung di baris PDF...",
                                            style = TextStyle(
                                                fontSize = 15.sp,
                                                fontFamily = AmiriFontFamily,
                                                color = Color(0xFFB5A995),
                                                textAlign = TextAlign.Right,
                                                textDirection = TextDirection.Rtl
                                            )
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    } else {
                        // Inactive Line: Render typewritten ink text
                        val lineText = item.transcription?.text ?: ""
                        if (lineText.isNotEmpty()) {
                            Text(
                                text = lineText,
                                style = TextStyle(
                                    fontSize = 17.sp,
                                    fontFamily = AmiriFontFamily,
                                    color = Color(0xFF2C241B),
                                    textAlign = TextAlign.Right,
                                    textDirection = TextDirection.Rtl,
                                    lineHeight = 25.sp
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            Text(
                                text = "— . . . . . . . . . . . . . . . . . . . . —",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    color = Color(0xFFD3CABE),
                                    textAlign = TextAlign.Right,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                    }
                }
            }

            // Status Indicator Dot
            val statusColor = when (item.transcription?.status) {
                "completed" -> StatusCompleted
                "unclear" -> StatusUnclear
                "annotated" -> StatusAnnotated
                else -> Color(0xFFD1C7B7)
            }

            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
        }

        // Ruled Notebook / PDF Line Guide Underneath
        HorizontalDivider(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 42.dp),
            thickness = 0.8.dp,
            color = if (isActive) Color(0xFFF59E0B).copy(alpha = 0.6f) else Color(0xFFE8E0D0)
        )
    }
}

@Composable
private fun AlternativeReadingsBar(
    alternatives: List<String>,
    onApply: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF2A241D),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4A3E2D))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Alternatif AI:",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = GoldAmber,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            alternatives.forEach { alt ->
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onApply(alt) },
                    color = Color(0xFF3B3226),
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldAmberDark)
                ) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        Text(
                            text = alt,
                            style = TextStyle(
                                fontFamily = AmiriFontFamily,
                                fontSize = 13.sp,
                                color = GoldAmberLight,
                                textDirection = TextDirection.Rtl
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PdfPageFooter(
    folioCode: String,
    totalLines: Int
) {
    Text(
        text = "Folio $folioCode • $totalLines baris",
        style = MaterialTheme.typography.labelSmall.copy(
            color = Color(0xFF999999),
            fontSize = 9.sp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        textAlign = TextAlign.Center
    )
}
