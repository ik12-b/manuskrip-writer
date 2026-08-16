package com.example.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.example.data.model.LineEntity
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Utility untuk pipeline HTR berbasis gambar sungguhan.
 *
 * Sebelumnya (bug v2.0 aplikasi ini): HTR mengirim `line.originalScriptText` (teks yang
 * SUDAH diketahui di database) ke Gemini dan minta "ditranskripsikan" — hasilnya cuma
 * teks yang di-echo balik oleh LLM, bukan OCR sungguhan dari foto manuskrip.
 *
 * Fungsi di sini memotong baris yang relevan dari foto folio (pakai bounding box
 * fraksional yang sudah ada di [LineEntity]) supaya yang dikirim ke model benar-benar
 * gambar tulisan tangan, bukan teks jawabannya.
 */
object BitmapUtils {

    /**
     * Crop bagian baris tertentu dari foto folio penuh berdasarkan bounding box
     * fraksional (0f..1f) yang tersimpan di [LineEntity].
     */
    fun cropLineFromFolio(folioImagePath: String, line: LineEntity): Bitmap? {
        return try {
            val fullBitmap = BitmapFactory.decodeFile(folioImagePath) ?: return null
            val w = fullBitmap.width
            val h = fullBitmap.height

            // Beri padding kecil di sekitar bbox (30% tinggi baris) supaya tidak
            // memotong tashkil di atas huruf atau ekor huruf (ي، ق، ن) di bawah baris.
            val padding = line.bboxHeight * 0.3f
            val top = ((line.bboxTop - padding) * h).roundToInt().coerceIn(0, h - 1)
            val bottom = ((line.bboxTop + line.bboxHeight + padding) * h).roundToInt().coerceIn(top + 1, h)
            val left = (line.bboxLeft * w).roundToInt().coerceIn(0, w - 1)
            val right = ((line.bboxLeft + line.bboxWidth) * w).roundToInt().coerceIn(left + 1, w)

            val cropped = Bitmap.createBitmap(fullBitmap, left, top, right - left, bottom - top)
            if (cropped !== fullBitmap) fullBitmap.recycle()
            cropped
        } catch (e: Exception) {
            null
        }
    }

    /** Encode bitmap ke JPEG base64, di-downscale agar hemat kuota & payload API. */
    fun toBase64Jpeg(bitmap: Bitmap, maxDimension: Int = 1024, quality: Int = 85): String {
        val scale = min(1f, maxDimension.toFloat() / max(bitmap.width, bitmap.height))
        val scaled = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).roundToInt().coerceAtLeast(1),
                (bitmap.height * scale).roundToInt().coerceAtLeast(1),
                true
            )
        } else {
            bitmap
        }
        val outputStream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        if (scaled !== bitmap) scaled.recycle()
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    /**
     * Salin foto yang dipilih user (content:// Uri dari photo picker/kamera) ke
     * penyimpanan internal aplikasi, supaya path-nya tetap valid meski izin akses
     * content:// sementara dari picker sudah dicabut sistem setelah aplikasi ditutup.
     * Mengembalikan absolute path file hasil salinan, atau null jika gagal.
     */
    fun persistPickedImage(context: Context, sourceUri: Uri, folioId: String): String? {
        return try {
            val dir = File(context.filesDir, "folio_images").apply { mkdirs() }
            val destFile = File(dir, "$folioId.jpg")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return null
            destFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}
