package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldAmber
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SlateParchmentBg
import com.example.ui.theme.StatusAnnotated
import com.example.ui.theme.StatusCompleted
import com.example.ui.theme.StatusDraft
import com.example.ui.theme.StatusUnclear
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ManuscriptStatusBar(
    isWriteMode: Boolean,
    currentLineIndex: Int,
    totalLines: Int,
    lineStatus: String,
    onToggleWriteMode: () -> Unit,
    onPreviousLine: () -> Unit,
    onNextLine: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("manuscript_status_bar"),
        color = ObsidianBg,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Mode Indicator Pill (Mode Baca vs Mode Menulis)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isWriteMode) Color(0x33F59E0B) else Color(0x3310B981)
                    )
                    .clickable { onToggleWriteMode() }
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (isWriteMode) GoldAmber else StatusCompleted
                        )
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = if (isWriteMode) "MODE MENULIS" else "MODE BACA",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isWriteMode) GoldAmber else StatusCompleted
                    )
                )

                Spacer(modifier = Modifier.width(4.dp))

                Icon(
                    imageVector = if (isWriteMode) Icons.Default.Edit else Icons.Default.Visibility,
                    contentDescription = null,
                    tint = if (isWriteMode) GoldAmber else StatusCompleted,
                    modifier = Modifier.size(12.dp)
                )
            }

            // Status Tag for current line
            val (statusBg, statusFg, statusLabel) = when (lineStatus) {
                "completed" -> Triple(Color(0x3310B981), StatusCompleted, "✓ Selesai")
                "unclear" -> Triple(Color(0x33EF4444), StatusUnclear, "❓ Tidak Jelas")
                "annotated" -> Triple(Color(0x33A855F7), StatusAnnotated, "📝 Catatan")
                else -> Triple(Color(0x33F59E0B), StatusDraft, "✎ Draft")
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(statusBg)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = statusFg,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            // Quick Line Stepper ◀ ▶
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onPreviousLine,
                    enabled = currentLineIndex > 0,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("prev_line_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Baris Sebelumnya",
                        tint = if (currentLineIndex > 0) TextPrimary else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = "${currentLineIndex + 1}/$totalLines",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(horizontal = 2.dp)
                )

                IconButton(
                    onClick = onNextLine,
                    enabled = currentLineIndex < totalLines - 1,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("next_line_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Baris Berikutnya",
                        tint = if (currentLineIndex < totalLines - 1) TextPrimary else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
