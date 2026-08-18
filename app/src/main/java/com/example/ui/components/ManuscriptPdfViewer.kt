package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
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
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ParchmentBorder
import com.example.ui.theme.ParchmentHighlight
import com.example.ui.theme.ParchmentPaper
import com.example.ui.theme.SlateParchmentBg
import com.example.ui.theme.StatusAnnotated
import com.example.ui.theme.StatusCompleted
import com.example.ui.theme.StatusDraft
import com.example.ui.theme.StatusUnclear
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class ManuscriptFilterMode(val label: String) {
    ORIGINAL("Parchment Alami"),
    HIGH_CONTRAST("Kontras Tinggi"),
    SEPIA_VINTAGE("Sepia Kuno")
}

@Composable
fun ManuscriptPdfViewer(
    document: DocumentEntity?,
    folio: FolioEntity?,
    lines: List<LineWithTranscription>,
    currentLineIndex: Int,
    syncController: PdfLinkedSyncController,
    writingProgressFraction: Float = 0f, // 0.0f (kanan) -> 1.0f (kiri) saat mengetik Arab
    onLineSelected: (Int) -> Unit,
    onAttachPhoto: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showBoundingBoxes by remember { mutableStateOf(true) }
    var filterMode by remember { mutableStateOf(ManuscriptFilterMode.ORIGINAL) }
    var autoFollowTyping by remember { mutableStateOf(true) }

    val density = LocalDensity.current

    // Virtual PDF Dimensions in DP (Besar seperti dokumen PDF asli)
    val pdfPageWidthDp = 580.dp
    val pdfPageHeightDp = 860.dp

    // Auto-center panel FOTO secara vertikal & horizontal ke baris yang sedang aktif.
    // PENTING: hanya bereaksi ke currentLineIndex (ganti baris), BUKAN lagi ke
    // writingProgressFraction (progres mengetik). Foto manuskrip itu statis — tidak
    // ada alasan foto ikut bergeser tiap kali user mengetik huruf baru di panel
    // bawah. Ini yang menyebabkan bug "foto keluar layout waktu ngetik" sebelumnya.
    //
    // Juga SENGAJA menulis langsung ke sharedPanX/Y (bukan lewat
    // syncController.triggerEdgeScroll()) — triggerEdgeScroll() sekarang khusus
    // dipakai carriage-shift kertas di panel bawah (menulis ke typewriterAutoPanX/Y),
    // dan panel foto ini tidak pernah membaca variabel itu. Kalau tetap dipanggil
    // lewat triggerEdgeScroll di sini, panel foto tidak akan pernah auto-center lagi
    // ke baris aktif — persis bug "area hitam besar, foto nyangkut di pojok".
    LaunchedEffect(currentLineIndex, autoFollowTyping) {
        if (autoFollowTyping && lines.isNotEmpty()) {
            val safeIdx = currentLineIndex.coerceIn(0, lines.size - 1)
            val lineItem = lines[safeIdx]

            val targetYPercent = lineItem.line.bboxTop + (lineItem.line.bboxHeight / 2f)
            val targetXPercent = lineItem.line.bboxLeft + (lineItem.line.bboxWidth / 2f)

            with(density) {
                val pageHeightPx = pdfPageHeightDp.toPx()
                val pageWidthPx = pdfPageWidthDp.toPx()

                val rawTargetY = -((targetYPercent - 0.5f) * pageHeightPx * syncController.topZoomScale)
                val newPanY = rawTargetY.coerceIn(-pageHeightPx * 0.85f, pageHeightPx * 0.85f)

                val rawTargetX = -((targetXPercent - 0.5f) * pageWidthPx * syncController.topZoomScale)
                val newPanX = rawTargetX.coerceIn(-pageWidthPx * 0.85f, pageWidthPx * 0.85f)

                syncController.sharedPanX = newPanX
                syncController.sharedPanY = newPanY
            }
        }
    }

    // Smooth animated panning and zoom positions
    val animatedPanX by animateFloatAsState(
        targetValue = syncController.sharedPanX,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "top_pdf_pan_x"
    )
    val animatedPanY by animateFloatAsState(
        targetValue = syncController.sharedPanY,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "top_pdf_pan_y"
    )
    val animatedZoom by animateFloatAsState(
        targetValue = syncController.topZoomScale,
        animationSpec = tween(durationMillis = 220),
        label = "top_pdf_zoom"
    )

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("manuscript_pdf_viewer"),
        color = Color(0xFF13110E)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // PDF Viewer Control Header Bar
            PdfViewerToolbar(
                document = document,
                folio = folio,
                zoomScale = syncController.topZoomScale,
                isLinked = syncController.isLinked,
                showBoundingBoxes = showBoundingBoxes,
                filterMode = filterMode,
                autoFollowTyping = autoFollowTyping,
                onZoomIn = { syncController.onPinchZoom("top", 1.2f) },
                onZoomOut = { syncController.onPinchZoom("top", 0.83f) },
                onResetZoom = { syncController.resetZoomAndPan() },
                onAutoAlign = { syncController.autoAlignAndCenter() },
                onToggleLink = { syncController.toggleLink() },
                onToggleBoundingBoxes = { showBoundingBoxes = !showBoundingBoxes },
                onToggleAutoFollow = { autoFollowTyping = !autoFollowTyping },
                onAttachPhoto = onAttachPhoto,
                onToggleFilter = {
                    filterMode = when (filterMode) {
                        ManuscriptFilterMode.ORIGINAL -> ManuscriptFilterMode.HIGH_CONTRAST
                        ManuscriptFilterMode.HIGH_CONTRAST -> ManuscriptFilterMode.SEPIA_VINTAGE
                        ManuscriptFilterMode.SEPIA_VINTAGE -> ManuscriptFilterMode.ORIGINAL
                    }
                }
            )

            // PDF Manuscript Zoomable & Pinchable Canvas Container
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF0C0A09))
                    .clip(RoundedCornerShape(0.dp))
                    .pointerInput(syncController.isLinked) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            // Linked or independent pinch-to-zoom & synchronized pan with boundaries
                            val containerW = size.width.toFloat()
                            val containerH = size.height.toFloat()
                            val pageW = with(density) { pdfPageWidthDp.toPx() }
                            val pageH = with(density) { pdfPageHeightDp.toPx() }

                            syncController.onPinchZoom("top", zoom)
                            syncController.onPanScroll(
                                source = "top",
                                deltaX = pan.x,
                                deltaY = pan.y,
                                containerWidth = containerW,
                                containerHeight = containerH,
                                contentWidth = pageW,
                                contentHeight = pageH
                            )
                            
                            // User manual drag pauses auto follow
                            if (pan.getDistance() > 12f) {
                                autoFollowTyping = false
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                // The Actual Large-Scale PDF Document Sheet
                Box(
                    modifier = Modifier
                        .size(pdfPageWidthDp, pdfPageHeightDp)
                        .graphicsLayer {
                            scaleX = animatedZoom
                            scaleY = animatedZoom
                            translationX = animatedPanX
                            translationY = animatedPanY
                        }
                        .shadow(16.dp, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when (filterMode) {
                                ManuscriptFilterMode.ORIGINAL -> Color(0xFF231E18)
                                ManuscriptFilterMode.HIGH_CONTRAST -> Color(0xFF141414)
                                ManuscriptFilterMode.SEPIA_VINTAGE -> Color(0xFF2B2013)
                            }
                        )
                        .border(2.dp, Color(0xFF78350F).copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                ) {
                    if (folio?.imageUrl != null) {
                        // === FOTO MANUSKRIP ASLI ===
                        // Sebelumnya (bug v2.0): panel ini selalu merender originalScriptText
                        // (teks yang sudah diketahui di DB) sebagai "simulasi facsimile" —
                        // tidak pernah menampilkan foto asli sama sekali. Sekarang, kalau
                        // folio sudah punya foto (lihat tombol 📷 di toolbar), foto itu yang
                        // ditampilkan, dan tap pada kotak bbox tiap baris memilih baris itu.
                        AsyncImage(
                            model = folio.imageUrl,
                            contentDescription = "Foto asli folio ${folio.folioNumber}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        if (showBoundingBoxes) {
                            lines.forEachIndexed { index, item ->
                                val isActive = index == currentLineIndex
                                val l = item.line
                                Box(
                                    modifier = Modifier
                                        .offset(
                                            x = (l.bboxLeft * pdfPageWidthDp.value).dp,
                                            y = (l.bboxTop * pdfPageHeightDp.value).dp
                                        )
                                        .size(
                                            width = (l.bboxWidth * pdfPageWidthDp.value).dp,
                                            height = (l.bboxHeight * pdfPageHeightDp.value).dp
                                        )
                                        .border(
                                            width = if (isActive) 2.dp else 1.dp,
                                            color = if (isActive) GoldAmber else Color.White.copy(alpha = 0.4f),
                                            shape = RoundedCornerShape(2.dp)
                                        )
                                        .background(if (isActive) GoldAmber.copy(alpha = 0.18f) else Color.Transparent)
                                        .clickable {
                                            autoFollowTyping = true
                                            onLineSelected(index)
                                        }
                                        .testTag("manuscript_photo_line_${index + 1}")
                                )
                            }
                        }
                    } else {
                        // === FALLBACK: simulasi facsimile (dipakai kalau folio belum ada foto) ===
                        // PDF Background Paper Texture & Classical Margin Lines
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Classical Manuscript Gold/Red Dual Border Lines
                            val borderInset = 28.dp.toPx()
                            drawRect(
                                color = Color(0xFFB45309).copy(alpha = 0.6f),
                                topLeft = Offset(borderInset, borderInset),
                                size = Size(size.width - borderInset * 2, size.height - borderInset * 2),
                                style = Stroke(width = 2.5f)
                            )
                            drawRect(
                                color = Color(0xFFD97706).copy(alpha = 0.4f),
                                topLeft = Offset(borderInset - 6.dp.toPx(), borderInset - 6.dp.toPx()),
                                size = Size(size.width - (borderInset - 6.dp.toPx()) * 2, size.height - (borderInset - 6.dp.toPx()) * 2),
                                style = Stroke(width = 1.2f)
                            )
                        }

                        // Content on the PDF Sheet
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 36.dp, vertical = 32.dp)
                        ) {
                            // PDF Illuminated Unwan / Header
                            ManuscriptIlluminatedHeader(
                                docTitle = document?.title ?: "Manuskrip Arab Kuno",
                                folioCode = folio?.folioNumber ?: "1r",
                                filterMode = filterMode
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Lines inside PDF
                            lines.forEachIndexed { index, item ->
                                val isActive = index == currentLineIndex
                                ManuscriptPdfLineItem(
                                    item = item,
                                    index = index,
                                    isActive = isActive,
                                    showBoundingBox = showBoundingBoxes,
                                    filterMode = filterMode,
                                    writingProgressFraction = if (isActive) writingProgressFraction else 0f,
                                    onSelect = {
                                        autoFollowTyping = true
                                        onLineSelected(index)
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // PDF Colophon Seal
                            ManuscriptPdfFooter(
                                folio = folio,
                                totalLines = lines.size
                            )
                        }

                        // Peringatan kecil: ini bukan foto asli
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp),
                            color = Color(0xFF7C2D12).copy(alpha = 0.85f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "⚠ Belum ada foto — tampilan simulasi",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFFFED7AA),
                                    fontSize = 8.5.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                // Linked Sync & Edge Auto-Scroll HUD Indicator
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (syncController.isLinked) {
                        Surface(
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

                    if (writingProgressFraction > 0.65f) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xDD7C2D12),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF97316))
                        ) {
                            Text(
                                text = "Auto-Geser Tepi Layar ◀",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFFFFEDD5),
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PdfViewerToolbar(
    document: DocumentEntity?,
    folio: FolioEntity?,
    zoomScale: Float,
    isLinked: Boolean,
    showBoundingBoxes: Boolean,
    filterMode: ManuscriptFilterMode,
    autoFollowTyping: Boolean,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onResetZoom: () -> Unit,
    onAutoAlign: () -> Unit,
    onToggleLink: () -> Unit,
    onToggleBoundingBoxes: () -> Unit,
    onToggleAutoFollow: () -> Unit,
    onToggleFilter: () -> Unit,
    onAttachPhoto: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        color = Color(0xFF1A1612),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF382D20))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // PDF Document & Page Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "${folio?.folioNumber ?: "Folio 1r"}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = GoldAmberLight,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Quick Actions: Linked Sync Toggle, Auto-Follow Lock, Filter, Pinch-Zoom Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Linked Sync Toggle Button (Links Top & Bottom Zoom + Scroll)
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onToggleLink() },
                    color = if (isLinked) Color(0xFF0C4A6E) else Color(0xFF2C241B),
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isLinked) Color(0xFF38BDF8) else Color(0xFF52402D)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isLinked) Icons.Default.Link else Icons.Default.LinkOff,
                            contentDescription = "Tautkan Sync",
                            tint = if (isLinked) Color(0xFF7DD3FC) else TextMuted,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (isLinked) "Taut" else "Lepas",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isLinked) Color(0xFFBAE6FD) else TextMuted,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                // Auto Follow Toggle
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onToggleAutoFollow() },
                    color = if (autoFollowTyping) IndigoViolet.copy(alpha = 0.3f) else Color(0xFF2C241B),
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (autoFollowTyping) IndigoViolet else Color(0xFF52402D)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (autoFollowTyping) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = "Auto Follow",
                            tint = if (autoFollowTyping) IndigoVioletLight else TextMuted,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (autoFollowTyping) "Kunci" else "Bebas",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (autoFollowTyping) IndigoVioletLight else TextMuted,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                // Filter Mode Chip
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onToggleFilter() },
                    color = Color(0xFF2C241B),
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF52402D))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterVintage,
                            contentDescription = "Filter",
                            tint = GoldAmber,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = filterMode.label.take(7),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondary,
                                fontSize = 8.5.sp
                            )
                        )
                    }
                }

                // Lampirkan Foto Folio Asli
                IconButton(
                    onClick = onAttachPhoto,
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Lampirkan Foto Folio",
                        tint = Color(0xFF4ADE80),
                        modifier = Modifier.size(15.dp)
                    )
                }

                // Overlay Segment Toggle
                IconButton(
                    onClick = onToggleBoundingBoxes,
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = if (showBoundingBoxes) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle Bounding Box",
                        tint = if (showBoundingBoxes) GoldAmber else TextMuted,
                        modifier = Modifier.size(15.dp)
                    )
                }

                // Zoom Out
                IconButton(
                    onClick = onZoomOut,
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Zoom Out",
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Zoom Level
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

                // Zoom In
                IconButton(
                    onClick = onZoomIn,
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Zoom In",
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Reset Zoom
                IconButton(
                    onClick = onResetZoom,
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = "Reset Zoom",
                        tint = GoldAmber,
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Auto-Align PDF Sheet
                IconButton(
                    onClick = onAutoAlign,
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CenterFocusStrong,
                        contentDescription = "Auto-Align PDF",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ManuscriptIlluminatedHeader(
    docTitle: String,
    folioCode: String,
    filterMode: ManuscriptFilterMode
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (filterMode) {
                ManuscriptFilterMode.ORIGINAL -> Color(0xFF2E2419)
                ManuscriptFilterMode.HIGH_CONTRAST -> Color(0xFF1B1B1B)
                ManuscriptFilterMode.SEPIA_VINTAGE -> Color(0xFF382A18)
            }
        ),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            Brush.horizontalGradient(listOf(Color(0xFFB45309), GoldAmber, Color(0xFFB45309)))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "﷽",
                style = TextStyleArabicManuscript.copy(
                    fontSize = 22.sp,
                    color = GoldAmberLight,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "⚜️ $docTitle [$folioCode] ⚜️",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = GoldAmber.copy(alpha = 0.9f),
                    fontSize = 10.sp,
                    letterSpacing = 0.8.sp
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ManuscriptPdfLineItem(
    item: LineWithTranscription,
    index: Int,
    isActive: Boolean,
    showBoundingBox: Boolean,
    filterMode: ManuscriptFilterMode,
    writingProgressFraction: Float,
    onSelect: () -> Unit
) {
    val statusColor = when (item.transcription?.status) {
        "completed" -> StatusCompleted
        "unclear" -> StatusUnclear
        "annotated" -> StatusAnnotated
        else -> StatusDraft
    }

    val activeBorderColor by animateColorAsState(
        targetValue = if (isActive) GoldAmber else if (showBoundingBox) Color(0x33B45309) else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "line_border_color"
    )

    val activeBgColor by animateColorAsState(
        targetValue = if (isActive) {
            Color(0x33F59E0B)
        } else {
            when (filterMode) {
                ManuscriptFilterMode.ORIGINAL -> Color(0x221B1713)
                ManuscriptFilterMode.HIGH_CONTRAST -> Color(0x22111111)
                ManuscriptFilterMode.SEPIA_VINTAGE -> Color(0x33281E12)
            }
        },
        animationSpec = tween(durationMillis = 200),
        label = "line_bg_color"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(activeBgColor)
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = activeBorderColor,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable { onSelect() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("pdf_manuscript_line_${index + 1}")
    ) {
        // Active Line Real-time Typing Cursor Tracker in Top Manuscript
        if (isActive && writingProgressFraction > 0f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(4.dp))
            ) {
                // Synchronized Laser Marker over the Arabic text character
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(4.dp)
                        .align(Alignment.CenterEnd)
                        // Translate marker RTL from right (0%) to left (100%)
                        .graphicsLayer {
                            translationX = -(size.width * writingProgressFraction * 0.85f)
                        }
                        .background(Color(0xFF38BDF8).copy(alpha = 0.8f))
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Line Number Tag & Bounding Box Coordinates Indicator
            Column(horizontalAlignment = Alignment.Start) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(if (isActive) 22.dp else 18.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isActive) GoldAmberDark else Color(0xFF332619)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isActive) Color.White else GoldAmberLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isActive) 11.sp else 9.sp
                            )
                        )
                    }

                    if (isActive) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(3.dp),
                            color = Color(0x446366F1),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, IndigoViolet)
                        ) {
                            Text(
                                text = "SINKRON AKTIF",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 8.sp,
                                    color = Color(0xFFA5B4FC),
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                if (showBoundingBox) {
                    Text(
                        text = "pos: y=${(item.line.bboxTop * 100).toInt()}% h=${(item.line.bboxHeight * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.sp,
                            color = Color(0xFF78624C)
                        ),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // Arabic Script Manuscript Line Rendering (Facsimile simulation)
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = item.line.originalScriptText,
                        style = TextStyleArabicManuscript.copy(
                            fontSize = if (isActive) 22.sp else 19.sp,
                            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                            color = when (filterMode) {
                                ManuscriptFilterMode.ORIGINAL -> if (isActive) GoldAmberLight else Color(0xFFE2D6B5)
                                ManuscriptFilterMode.HIGH_CONTRAST -> if (isActive) Color.White else Color(0xFFD4D4D8)
                                ManuscriptFilterMode.SEPIA_VINTAGE -> if (isActive) Color(0xFFFDE68A) else Color(0xFFDFC69B)
                            },
                            textAlign = TextAlign.Right,
                            textDirection = TextDirection.Rtl,
                            lineHeight = 30.sp
                        )
                    )
                }
            }

            // Status indicator pip
            Box(
                modifier = Modifier
                    .padding(start = 6.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
        }
    }
}

@Composable
private fun ManuscriptPdfFooter(
    folio: FolioEntity?,
    totalLines: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "✤ ✤ ✤",
            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF785E43))
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Akhir Halaman [Folio ${folio?.folioNumber ?: "1r"} • Total $totalLines Baris Manuskrip]",
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color(0xFF8C7358),
                fontSize = 9.sp
            )
        )
    }
}

private val TextStyleArabicManuscript = TextStyle(
    fontFamily = AmiriFontFamily,
    textDirection = TextDirection.Rtl
)
