package com.example.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.ui.theme.GoldAmber
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SlateParchmentBg
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.utils.PdfImportUtils

@Composable
fun AddDocumentDialog(
    pickedFileUri: Uri?,
    onPickFile: () -> Unit,
    onAddDocument: (title: String, repository: String, period: String, scriptType: String, lineCount: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var repository by remember { mutableStateOf("") }
    var period by remember { mutableStateOf("") }
    var scriptType by remember { mutableStateOf("Naskh") }
    var lineCountText by remember { mutableStateOf("8") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                .testTag("add_document_dialog"),
            color = SlateParchmentBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = GoldAmber,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tambah Naskah Manuskrip",
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

                // Foto atau PDF Manuskrip Asli — WAJIB. Menggantikan kotak "ketik ulang
                // teks" versi lama: sekarang sumber manuskrip harus foto/scan sungguhan,
                // dan teksnya ditranskripsi belakangan, bukan diketik duluan.
                // PDF multi-halaman didukung: satu halaman PDF = satu folio.
                Text(
                    text = "Foto / PDF Hasil Scan Manuskrip *",
                    style = MaterialTheme.typography.labelLarge.copy(color = TextPrimary),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                val context = LocalContext.current
                val isPickedPdf = remember(pickedFileUri) {
                    pickedFileUri?.let { PdfImportUtils.isPdf(context, it) } ?: false
                }
                if (pickedFileUri != null && isPickedPdf) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x1A22C55E))
                            .border(1.dp, Color(0xFF22C55E).copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = Color(0xFF22C55E),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "File PDF dipilih — tiap halaman jadi satu folio",
                            style = MaterialTheme.typography.labelMedium.copy(color = TextPrimary)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    TextButton(onClick = onPickFile) {
                        Text("Ganti File", color = GoldAmber)
                    }
                } else if (pickedFileUri != null) {
                    Column {
                        AsyncImage(
                            model = pickedFileUri,
                            contentDescription = "Pratinjau foto manuskrip",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.75f)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        TextButton(onClick = onPickFile) {
                            Text("Ganti Foto", color = GoldAmber)
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x1AD4AF37))
                            .border(1.dp, GoldAmber.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .clickable(onClick = onPickFile),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = GoldAmber,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ketuk untuk pilih foto folio atau file PDF hasil scan",
                            style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Naskah / Kitab") },
                    placeholder = { Text("misal: Mukhtasar al-Jabr wa-l-Muqabala") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldAmber,
                        unfocusedBorderColor = SurfaceBorder
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Repository Input
                OutlinedTextField(
                    value = repository,
                    onValueChange = { repository = it },
                    label = { Text("Repositori / Perpustakaan") },
                    placeholder = { Text("misal: Bodleian Library, Oxford (MS Hunt 214)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldAmber,
                        unfocusedBorderColor = SurfaceBorder
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Date and Khat / Script Type
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = period,
                        onValueChange = { period = it },
                        label = { Text("Periode / Penanggalan") },
                        placeholder = { Text("misal: Abad ke-13 M") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldAmber,
                            unfocusedBorderColor = SurfaceBorder
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = scriptType,
                        onValueChange = { scriptType = it },
                        label = { Text("Jenis Khat") },
                        placeholder = { Text("Naskh / Kufi / Thuluth") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldAmber,
                            unfocusedBorderColor = SurfaceBorder
                        ),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Jumlah baris — dipakai untuk membagi rata tinggi foto jadi kotak
                // per-baris (bounding box) sebagai titik awal transkripsi. Belum ada
                // deteksi baris otomatis, jadi pembagian ini murni perkiraan rata.
                OutlinedTextField(
                    value = lineCountText,
                    onValueChange = { new -> if (new.all { it.isDigit() } && new.length <= 3) lineCountText = new },
                    label = { Text("Jumlah Baris per Halaman") },
                    placeholder = { Text("misal: 8") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldAmber,
                        unfocusedBorderColor = SurfaceBorder
                    ),
                    singleLine = true
                )
                Text(
                    text = "Tiap halaman (foto atau per halaman PDF) dibagi rata jadi kotak per baris — nanti bisa disesuaikan saat transkripsi.",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary),
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onAddDocument(
                                title,
                                repository,
                                period,
                                scriptType,
                                lineCountText.toIntOrNull() ?: 0
                            )
                        },
                        enabled = pickedFileUri != null && title.isNotBlank() && (lineCountText.toIntOrNull() ?: 0) > 0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldAmber,
                            contentColor = ObsidianBg
                        )
                    ) {
                        Text("Simpan Manuskrip", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
