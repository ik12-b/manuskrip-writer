package com.example.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.File
import java.io.FileOutputStream

/**
 * Import PDF hasil scan manuskrip (mis. dari scanner perpustakaan/arsip) sebagai
 * beberapa folio sekaligus — satu halaman PDF menjadi satu foto folio.
 *
 * Pakai android.graphics.pdf.PdfRenderer bawaan Android (API 21+), sepenuhnya
 * offline, tidak butuh library pihak ketiga.
 */
object PdfImportUtils {

    /** Render tiap halaman PDF ke Bitmap, dalam resolusi cukup tinggi untuk dibaca HTR. */
    private const val RENDER_DPI_SCALE = 2.5f // ~180 DPI untuk PDF standar 72 DPI/point

    data class ImportedPage(val pageIndex: Int, val imagePath: String)

    /**
     * Salin+render setiap halaman PDF dari [pdfUri] ke file JPEG terpisah di storage
     * internal aplikasi. Mengembalikan daftar path gambar per halaman (urut), atau
     * null kalau file bukan PDF valid / gagal dibuka.
     */
    fun importPdfPages(context: Context, pdfUri: Uri, docId: String): List<ImportedPage>? {
        val dir = File(context.filesDir, "folio_images").apply { mkdirs() }

        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        return try {
            pfd = context.contentResolver.openFileDescriptor(pdfUri, "r") ?: return null
            renderer = PdfRenderer(pfd)

            val results = mutableListOf<ImportedPage>()
            for (pageIndex in 0 until renderer.pageCount) {
                renderer.openPage(pageIndex).use { page ->
                    val width = (page.width * RENDER_DPI_SCALE).toInt().coerceAtLeast(1)
                    val height = (page.height * RENDER_DPI_SCALE).toInt().coerceAtLeast(1)
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    // Latar putih dulu — halaman PDF hasil scan biasanya punya area transparan
                    // di tepi yang kalau dibiarkan default (hitam) bikin hasil OCR/tampilan aneh.
                    bitmap.eraseColor(android.graphics.Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                    val destFile = File(dir, "${docId}_p${pageIndex + 1}.jpg")
                    FileOutputStream(destFile).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                    }
                    bitmap.recycle()
                    results.add(ImportedPage(pageIndex, destFile.absolutePath))
                }
            }
            results
        } catch (e: Exception) {
            null
        } finally {
            renderer?.close()
            pfd?.close()
        }
    }

    /** Cek cepat apakah Uri yang dipilih user adalah file PDF, berdasarkan MIME type. */
    fun isPdf(context: Context, uri: Uri): Boolean {
        return context.contentResolver.getType(uri) == "application/pdf"
    }
}
