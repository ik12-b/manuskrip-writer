package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlin.math.max

@Stable
class PdfLinkedSyncController(
    initialLinked: Boolean = true,
    initialTopZoom: Float = 1.35f,
    initialBottomZoom: Float = 1.85f
) {
    var isLinked by mutableStateOf(initialLinked)
    var topZoomScale by mutableFloatStateOf(initialTopZoom)
    var bottomZoomScale by mutableFloatStateOf(initialBottomZoom)
    var sharedPanX by mutableFloatStateOf(0f)
    var sharedPanY by mutableFloatStateOf(0f)
    var isNearEdge by mutableStateOf(false)
    var lastSyncSource by mutableStateOf("system") // "top", "bottom", "typing", "system"

    fun onPinchZoom(source: String, zoomFactor: Float) {
        if (isLinked) {
            topZoomScale = (topZoomScale * zoomFactor).coerceIn(0.7f, 3.8f)
            bottomZoomScale = (bottomZoomScale * zoomFactor).coerceIn(0.8f, 4.0f)
        } else {
            if (source == "top") {
                topZoomScale = (topZoomScale * zoomFactor).coerceIn(0.7f, 3.8f)
            } else {
                bottomZoomScale = (bottomZoomScale * zoomFactor).coerceIn(0.8f, 4.0f)
            }
        }
        lastSyncSource = source
    }

    fun onPanScroll(
        source: String,
        deltaX: Float,
        deltaY: Float,
        containerWidth: Float = 0f,
        containerHeight: Float = 0f,
        contentWidth: Float = 0f,
        contentHeight: Float = 0f
    ) {
        val newPanX = sharedPanX + deltaX
        val newPanY = sharedPanY + deltaY

        // Auto-Align & Boundary Clamping: Never let PDF sheet drift completely out of view!
        if (containerWidth > 0f && containerHeight > 0f && contentWidth > 0f && contentHeight > 0f) {
            val zoom = if (source == "top") topZoomScale else bottomZoomScale
            val scaledW = contentWidth * zoom
            val scaledH = contentHeight * zoom

            val maxAllowX = max(60f, (scaledW - containerWidth) / 2f + containerWidth * 0.25f)
            val maxAllowY = max(80f, (scaledH - containerHeight) / 2f + containerHeight * 0.30f)

            sharedPanX = newPanX.coerceIn(-maxAllowX, maxAllowX)
            sharedPanY = newPanY.coerceIn(-maxAllowY, maxAllowY)
        } else {
            sharedPanX = newPanX.coerceIn(-600f, 600f)
            sharedPanY = newPanY.coerceIn(-900f, 900f)
        }

        lastSyncSource = source
    }

    fun triggerEdgeScroll(
        targetPanX: Float,
        targetPanY: Float,
        maxBoundX: Float = 500f,
        maxBoundY: Float = 800f
    ) {
        // Clamp to ensure edge auto-scroll stays within PDF margins
        sharedPanX = targetPanX.coerceIn(-maxBoundX, maxBoundX)
        sharedPanY = targetPanY.coerceIn(-maxBoundY, maxBoundY)
        isNearEdge = true
        lastSyncSource = "typing_edge"
    }

    fun autoAlignAndCenter() {
        sharedPanX = 0f
        sharedPanY = 0f
        isNearEdge = false
        lastSyncSource = "auto_align"
    }

    fun resetZoomAndPan() {
        topZoomScale = 1.35f
        bottomZoomScale = 1.85f
        sharedPanX = 0f
        sharedPanY = 0f
        isNearEdge = false
        lastSyncSource = "reset"
    }

    fun toggleLink() {
        isLinked = !isLinked
    }
}

@Composable
fun rememberPdfLinkedSyncController(
    initialLinked: Boolean = true,
    initialTopZoom: Float = 1.35f,
    initialBottomZoom: Float = 1.85f
): PdfLinkedSyncController {
    return remember {
        PdfLinkedSyncController(
            initialLinked = initialLinked,
            initialTopZoom = initialTopZoom,
            initialBottomZoom = initialBottomZoom
        )
    }
}
