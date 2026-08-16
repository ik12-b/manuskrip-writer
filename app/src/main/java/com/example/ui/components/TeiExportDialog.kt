package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.DocumentEntity
import com.example.data.model.FolioEntity
import com.example.data.model.LineWithTranscription
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.GoldAmber
import com.example.ui.theme.GoldAmberLight
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SlateParchmentBg
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.utils.PdfDocumentExporter

@Composable
fun TeiExportDialog(
    document: DocumentEntity?,
    folio: FolioEntity?,
    lines: List<LineWithTranscription>,
    teiXmlContent: String,
    markdownContent: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedFormatIndex by remember { mutableIntStateOf(0) }
    var copyStatus by remember { mutableStateOf<String?>(null) }
    var isGeneratingPdf by remember { mutableStateOf(false) }

    val formats = listOf("📄 Dokumen PDF Aligned", "TEI/XML (P5)", "Markdown", "Teks Arab")

    val currentContent = when (selectedFormatIndex) {
        1 -> teiXmlContent
        2 -> markdownContent
        3 -> {
            // Extract pure Arabic text
            markdownContent.lines()
                .filter { it.contains("**") }
                .map { it.substringAfter("**").substringBefore("**") }
                .joinToString("\n")
        }
        else -> ""
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                .testTag("tei_export_dialog"),
            color = SlateParchmentBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (selectedFormatIndex == 0) Icons.Default.PictureAsPdf else Icons.Default.DataObject,
                            contentDescription = null,
                            tint = if (selectedFormatIndex == 0) Color(0xFFEF4444) else GoldAmber,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ekspor Naskah & Transkripsi",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Format Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    formats.forEachIndexed { index, formatName ->
                        val isSelected = selectedFormatIndex == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) Color(0x33F59E0B) else SurfaceCard)
                                .border(
                                    1.dp,
                                    if (isSelected) GoldAmber else SurfaceBorder,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { selectedFormatIndex = index }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = formatName,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) GoldAmber else TextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Content Viewer
                if (selectedFormatIndex == 0) {
                    // PDF Document Export Overview Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ObsidianBg)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                            .padding(14.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Column {
                            // PDF Visual Summary Card
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF262019)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.PictureAsPdf,
                                            contentDescription = null,
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Dokumen PDF Matriks Terpadu (Aligned Edition)",
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    color = GoldAmberLight,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                            Text(
                                                text = "Format Standar A4 Cetak Filologi",
                                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = "Dokumen PDF yang dihasilkan menyatukan:",
                                        style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "• Kolom Kiri: Faksimili Simulasi Naskah Asli berserta Bounding Box & Tarikh.\n• Kolom Kanan: Teks Transkripsi Terverifikasi, Status Sah/Ragu, Skrip, dan Catatan Kritis.\n• Tata letak baris (Line-by-Line) terkunci lurus dan sejajar sempurna.",
                                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, lineHeight = 16.sp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Metadata Summary
                            Text(
                                text = "Detail Manuskrip yang Diekspor:",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = GoldAmber,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• Judul Naskah: ${document?.title ?: "-"}\n• Folio Aktif: ${folio?.folioNumber ?: "1r"} (${folio?.title ?: ""})\n• Jumlah Baris: ${lines.size} Baris\n• Repositori: ${document?.repository ?: "-"}\n• Skrip Kaligrafi: ${document?.scriptType ?: "-"}",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary, lineHeight = 18.sp)
                            )
                        }
                    }
                } else {
                    // Code Area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ObsidianBg)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = currentContent,
                            fontFamily = if (selectedFormatIndex == 1) FontFamily.Monospace else AmiriFontFamily,
                            fontSize = if (selectedFormatIndex == 1) 12.sp else 14.sp,
                            lineHeight = if (selectedFormatIndex == 1) 18.sp else 22.sp,
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (copyStatus != null) {
                        Text(
                            text = copyStatus ?: "",
                            style = MaterialTheme.typography.labelSmall.copy(color = GoldAmber)
                        )
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onDismiss) {
                            Text("Tutup", color = TextSecondary)
                        }

                        if (selectedFormatIndex == 0) {
                            // Action: Generate & Share PDF
                            Button(
                                onClick = {
                                    if (document != null && folio != null) {
                                        try {
                                            val file = PdfDocumentExporter.generateAlignedManuscriptPdf(
                                                context = context,
                                                document = document,
                                                folio = folio,
                                                lines = lines
                                            )
                                            PdfDocumentExporter.sharePdf(context, file)
                                            copyStatus = "PDF Berhasil Dibuat! ✓"
                                        } catch (e: Exception) {
                                            copyStatus = "Gagal: ${e.message}"
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFDC2626),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Buka / Bagikan PDF 📄", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("ManuScribe Export", currentContent)
                                    clipboard.setPrimaryClip(clip)
                                    copyStatus = "Tersalin ke Clipboard! ✓"
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldAmber,
                                    contentColor = ObsidianBg
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Salin Teks", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
