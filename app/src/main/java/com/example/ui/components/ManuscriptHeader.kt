package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DocumentEntity
import com.example.data.model.FolioEntity
import com.example.ui.theme.GoldAmber
import com.example.ui.theme.GoldAmberDark
import com.example.ui.theme.IndigoViolet
import com.example.ui.theme.IndigoVioletLight
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SlateParchmentBg
import com.example.ui.theme.StatusCompleted
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ManuscriptHeader(
    document: DocumentEntity?,
    folio: FolioEntity?,
    totalLines: Int,
    currentLineNumber: Int,
    pendingSyncCount: Int,
    isSyncing: Boolean,
    onOpenDocPicker: () -> Unit,
    onOpenExport: () -> Unit,
    onExportPdf: () -> Unit,
    onOpenAddDoc: () -> Unit,
    onSyncNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("manuscript_header"),
        color = SlateParchmentBg,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // App Logo and Document Selector
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onOpenDocPicker() }
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo Icon with Gradient
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(GoldAmberDark, IndigoViolet)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "م",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = document?.title ?: "ManuScribe Arab",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = "Ganti Naskah",
                                tint = GoldAmber,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Text(
                            text = if (folio != null) {
                                "${folio.title} • Baris $currentLineNumber dari $totalLines"
                            } else {
                                "Pilih Manuskrip Historis"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Action Buttons (Add, Sync, TEI Export)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Add Custom Document
                    IconButton(
                        onClick = onOpenAddDoc,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("add_doc_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Tambah Manuskrip",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Sync Queue Button with Badge
                    IconButton(
                        onClick = onSyncNow,
                        enabled = !isSyncing,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("sync_button")
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = GoldAmber
                            )
                        } else {
                            BadgedBox(
                                badge = {
                                    if (pendingSyncCount > 0) {
                                        Badge(
                                            containerColor = GoldAmber,
                                            contentColor = ObsidianBg
                                        ) {
                                            Text(
                                                text = if (pendingSyncCount > 99) "99+" else pendingSyncCount.toString(),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "Batch Sync Queue",
                                    tint = if (pendingSyncCount > 0) GoldAmber else TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // PDF Document Export Button (Direct One-Click)
                    FilledTonalIconButton(
                        onClick = onExportPdf,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = Color(0xFF3B1D1D),
                            contentColor = Color(0xFFF87171)
                        ),
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("export_pdf_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "Ekspor PDF Aligned",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // TEI / XML Export Button
                    FilledTonalIconButton(
                        onClick = onOpenExport,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = SurfaceCard,
                            contentColor = GoldAmber
                        ),
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("export_tei_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "Ekspor TEI/XML",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
