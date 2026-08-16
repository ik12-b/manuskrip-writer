package com.example.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.DocumentEntity
import com.example.data.model.FolioEntity
import com.example.data.model.LineWithTranscription
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfDocumentExporter {

    private const val PAGE_WIDTH = 595 // A4 standard width in points
    private const val PAGE_HEIGHT = 842 // A4 standard height in points
    private const val MARGIN = 36f

    fun generateAlignedManuscriptPdf(
        context: Context,
        document: DocumentEntity,
        folio: FolioEntity,
        lines: List<LineWithTranscription>
    ): File {
        val pdfDoc = PdfDocument()
        val linesPerPage = 6
        val totalPages = ((lines.size + linesPerPage - 1) / linesPerPage).coerceAtLeast(1)
        val dateStr = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID")).format(Date())

        val bgPaint = Paint().apply { color = Color.parseColor("#FDFBF7") }
        val headerBgPaint = Paint().apply { color = Color.parseColor("#1F1A15") }
        val colHeaderBgPaint = Paint().apply { color = Color.parseColor("#2C241B") }
        val goldBorderPaint = Paint().apply {
            color = Color.parseColor("#D97706")
            strokeWidth = 1.5f
            style = Paint.Style.STROKE
        }
        val dividerPaint = Paint().apply {
            color = Color.parseColor("#E5E7EB")
            strokeWidth = 0.8f
            style = Paint.Style.STROKE
        }
        val lineSeparatorPaint = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 0.6f
            style = Paint.Style.STROKE
        }

        // Text Paints
        val titlePaint = Paint().apply {
            color = Color.parseColor("#FDE68A")
            textSize = 13f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val subtitlePaint = Paint().apply {
            color = Color.parseColor("#D1D5DB")
            textSize = 8.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }
        val colTitlePaint = Paint().apply {
            color = Color.parseColor("#FDE68A")
            textSize = 9f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val lineNumPaint = Paint().apply {
            color = Color.parseColor("#78350F")
            textSize = 8f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }
        val originalArabicPaint = Paint().apply {
            color = Color.parseColor("#1C1917")
            textSize = 14f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        val transcriptionArabicPaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 14f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        val statusCompletedPaint = Paint().apply {
            color = Color.parseColor("#059669")
            textSize = 7.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val statusDraftPaint = Paint().apply {
            color = Color.parseColor("#D97706")
            textSize = 7.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val notesPaint = Paint().apply {
            color = Color.parseColor("#475569")
            textSize = 7.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC)
            isAntiAlias = true
        }
        val footerPaint = Paint().apply {
            color = Color.parseColor("#94A3B8")
            textSize = 7.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }

        // Facsimile column background
        val facsimileBgPaint = Paint().apply { color = Color.parseColor("#F5EFE6") }
        val transcriptionBgPaint = Paint().apply { color = Color.parseColor("#FFFFFF") }

        for (pageIndex in 0 until totalPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex + 1).create()
            val page = pdfDoc.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            // 1. Page Background & Border
            canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), bgPaint)
            val pageBorderRect = RectF(MARGIN - 10, MARGIN - 10, PAGE_WIDTH - MARGIN + 10, PAGE_HEIGHT - MARGIN + 10)
            canvas.drawRoundRect(pageBorderRect, 6f, 6f, goldBorderPaint)

            // 2. Header Banner
            val headerHeight = 62f
            val headerRect = RectF(MARGIN, MARGIN, PAGE_WIDTH - MARGIN, MARGIN + headerHeight)
            canvas.drawRoundRect(headerRect, 4f, 4f, headerBgPaint)
            
            canvas.drawText("﷽  DOKUMEN PENJELASAN & TRANSKRIPSI ALIGNED MANUSKRIP", MARGIN + 14f, MARGIN + 22f, titlePaint)
            canvas.drawText(
                "Naskah: ${document.title} | Folio: ${folio.folioNumber} | Tarikh: ${document.datePeriod} | Skrip: ${document.scriptType}",
                MARGIN + 14f,
                MARGIN + 38f,
                subtitlePaint
            )
            canvas.drawText(
                "Repositori: ${document.repository} | Diekspor: $dateStr",
                MARGIN + 14f,
                MARGIN + 52f,
                subtitlePaint
            )

            // 3. Dual Columns Setup
            val colTop = MARGIN + headerHeight + 12f
            val colHeaderHeight = 22f
            val contentBottom = PAGE_HEIGHT - MARGIN - 28f
            val contentHeight = contentBottom - (colTop + colHeaderHeight)
            val halfWidth = (PAGE_WIDTH - MARGIN * 2 - 12f) / 2f

            val leftColLeft = MARGIN
            val leftColRight = leftColLeft + halfWidth
            val rightColLeft = leftColRight + 12f
            val rightColRight = PAGE_WIDTH - MARGIN

            // Column Headers
            val leftHeaderRect = RectF(leftColLeft, colTop, leftColRight, colTop + colHeaderHeight)
            val rightHeaderRect = RectF(rightColLeft, colTop, rightColRight, colTop + colHeaderHeight)
            canvas.drawRoundRect(leftHeaderRect, 3f, 3f, colHeaderBgPaint)
            canvas.drawRoundRect(rightHeaderRect, 3f, 3f, colHeaderBgPaint)

            canvas.drawText("📜 1. FAKSIMILI NASKAH ASLI (SIMULASI)", leftColLeft + 8f, colTop + 14f, colTitlePaint)
            canvas.drawText("✍️ 2. HASIL TRANSKRIPSI TERSTRUKTUR", rightColLeft + 8f, colTop + 14f, colTitlePaint)

            // Column Content Backgrounds
            val leftContentRect = RectF(leftColLeft, colTop + colHeaderHeight, leftColRight, contentBottom)
            val rightContentRect = RectF(rightColLeft, colTop + colHeaderHeight, rightColRight, contentBottom)
            canvas.drawRoundRect(leftContentRect, 3f, 3f, facsimileBgPaint)
            canvas.drawRoundRect(rightContentRect, 3f, 3f, transcriptionBgPaint)

            canvas.drawRoundRect(leftContentRect, 3f, 3f, dividerPaint)
            canvas.drawRoundRect(rightContentRect, 3f, 3f, dividerPaint)

            // 4. Render Aligned Rows
            val startIndex = pageIndex * linesPerPage
            val endIndex = (startIndex + linesPerPage).coerceAtMost(lines.size)
            val linesInThisPage = endIndex - startIndex
            val rowHeight = contentHeight / linesPerPage.toFloat()

            for (i in 0 until linesInThisPage) {
                val lineIndex = startIndex + i
                val item = lines[lineIndex]
                val rowTop = (colTop + colHeaderHeight) + (i * rowHeight)
                val rowBottom = rowTop + rowHeight
                val rowMidY = rowTop + (rowHeight / 2f)

                // Row Separator Line
                if (i > 0) {
                    canvas.drawLine(leftColLeft, rowTop, leftColRight, rowTop, lineSeparatorPaint)
                    canvas.drawLine(rightColLeft, rowTop, rightColRight, rowTop, lineSeparatorPaint)
                }

                // --- LEFT COLUMN (FACSIMILE SIDE) ---
                // Line Number Tag
                canvas.drawText("[B.${lineIndex + 1}]", leftColLeft + 8f, rowTop + 16f, lineNumPaint)

                // Bounding Box Metadata
                val bboxStr = "y:${(item.line.bboxTop * 100).toInt()}% | h:${(item.line.bboxHeight * 100).toInt()}%"
                canvas.drawText(bboxStr, leftColLeft + 8f, rowTop + 28f, notesPaint)

                // Original Manuscript Script (Right Aligned RTL)
                val originalText = item.line.originalScriptText
                canvas.drawText(originalText, leftColRight - 10f, rowMidY + 4f, originalArabicPaint)

                // --- RIGHT COLUMN (TRANSCRIPTION SIDE) ---
                // Line Number
                canvas.drawText("Baris ${lineIndex + 1}:", rightColLeft + 8f, rowTop + 16f, lineNumPaint)

                // Status Tag & Confidence
                val statusText = when (item.transcription?.status) {
                    "completed" -> "✓ Terverifikasi Sah"
                    "unclear" -> "? Masih Ragu"
                    "annotated" -> "★ Catatan Filologis"
                    else -> "✎ Draf Transkripsi"
                }
                val paintForStatus = if (item.transcription?.status == "completed") statusCompletedPaint else statusDraftPaint
                val conf = item.transcription?.confidence ?: 0.90f
                canvas.drawText("$statusText (${(conf * 100).toInt()}%)", rightColLeft + 48f, rowTop + 16f, paintForStatus)

                // Transcribed Arabic Text
                val transcribedText = item.transcription?.text?.ifBlank { "— [Belum ditranskripsi] —" } ?: item.line.originalScriptText
                canvas.drawText(transcribedText, rightColRight - 10f, rowMidY + 4f, transcriptionArabicPaint)

                // Marginal Notes (if any)
                val notes = item.transcription?.notes
                if (!notes.isNullOrBlank()) {
                    val trimmedNotes = if (notes.length > 45) notes.take(42) + "..." else notes
                    canvas.drawText("Catatan: $trimmedNotes", rightColLeft + 8f, rowBottom - 6f, notesPaint)
                }
            }

            // 5. Page Footer
            val footerY = PAGE_HEIGHT - MARGIN + 2f
            canvas.drawText(
                "ManuScribe Arab Historical HTR Engine • Dokumen Bersinkronisasi Sisi-ke-Sisi",
                MARGIN,
                footerY,
                footerPaint
            )
            val pageNumStr = "Halaman ${pageIndex + 1} dari $totalPages"
            canvas.drawText(pageNumStr, PAGE_WIDTH - MARGIN - 70f, footerY, footerPaint)

            pdfDoc.finishPage(page)
        }

        // Write PDF to cache directory
        val fileName = "ManuScribe_${document.id}_folio_${folio.folioNumber}.pdf"
            .replace("[^a-zA-Z0-9._-]".toRegex(), "_")
        val exportFile = File(context.cacheDir, fileName)
        FileOutputStream(exportFile).use { outStream ->
            pdfDoc.writeTo(outStream)
        }
        pdfDoc.close()

        return exportFile
    }

    fun sharePdf(context: Context, pdfFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Dokumen Manuskrip & Transkripsi Aligned: ${pdfFile.name}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Buka atau Bagikan Dokumen PDF")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
