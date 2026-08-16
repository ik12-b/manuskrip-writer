package com.example.utils

import com.example.data.model.DocumentEntity
import com.example.data.model.FolioEntity
import com.example.data.model.LineWithTranscription
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TeiXmlExporter {

    fun exportToTeiP5Xml(
        document: DocumentEntity,
        folio: FolioEntity,
        lines: List<LineWithTranscription>
    ): String {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val sb = StringBuilder()

        sb.appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
        sb.appendLine("<TEI xmlns=\"http://www.tei-c.org/ns/1.0\" xml:lang=\"ar\">")
        sb.appendLine("  <teiHeader>")
        sb.appendLine("    <fileDesc>")
        sb.appendLine("      <titleStmt>")
        sb.appendLine("        <title>${escapeXml(document.title)}</title>")
        sb.appendLine("        <author>ManuScribe Arab Historical HTR Engine</author>")
        sb.appendLine("        <respStmt>")
        sb.appendLine("          <resp>Digital Scholarly Transcription &amp; Encoding</resp>")
        sb.appendLine("          <name>ManuScribe Hot 11 Play Edition</name>")
        sb.appendLine("        </respStmt>")
        sb.appendLine("      </titleStmt>")
        sb.appendLine("      <publicationStmt>")
        sb.appendLine("        <p>Digitized scholarly transcription exported on $currentDate</p>")
        sb.appendLine("      </publicationStmt>")
        sb.appendLine("      <sourceDesc>")
        sb.appendLine("        <msDesc>")
        sb.appendLine("          <msIdentifier>")
        sb.appendLine("            <repository>${escapeXml(document.repository)}</repository>")
        sb.appendLine("            <idno>${escapeXml(document.id)}</idno>")
        sb.appendLine("          </msIdentifier>")
        sb.appendLine("          <msContents>")
        sb.appendLine("            <summary>${escapeXml(document.description)}</summary>")
        sb.appendLine("          </msContents>")
        sb.appendLine("          <physDesc>")
        sb.appendLine("            <handDesc>")
        sb.appendLine("              <handNote script=\"${escapeXml(document.scriptType)}\"/>")
        sb.appendLine("            </handDesc>")
        sb.appendLine("          </physDesc>")
        sb.appendLine("          <history>")
        sb.appendLine("            <origin>")
        sb.appendLine("              <origDate>${escapeXml(document.datePeriod)}</origDate>")
        sb.appendLine("            </origin>")
        sb.appendLine("          </history>")
        sb.appendLine("        </msDesc>")
        sb.appendLine("      </sourceDesc>")
        sb.appendLine("    </fileDesc>")
        sb.appendLine("  </teiHeader>")
        sb.appendLine("  <text>")
        sb.appendLine("    <body>")
        sb.appendLine("      <div type=\"folio\" n=\"${escapeXml(folio.folioNumber)}\">")
        sb.appendLine("        <head>${escapeXml(folio.title)}</head>")

        lines.forEach { item ->
            val lineNum = item.line.lineNumber
            val text = item.transcription?.text?.ifBlank { item.line.originalScriptText } ?: item.line.originalScriptText
            val status = item.transcription?.status ?: "draft"
            val cert = when {
                (item.transcription?.confidence ?: 0f) >= 0.85f -> "high"
                (item.transcription?.confidence ?: 0f) >= 0.60f -> "medium"
                else -> "low"
            }
            val notes = item.transcription?.notes

            sb.append("        <lb n=\"$lineNum\" facs=\"#line-$lineNum\" cert=\"$cert\" status=\"$status\"/>")
            sb.append(escapeXml(text))
            if (!notes.isNullOrBlank()) {
                sb.append(" <note type=\"paleographical\">${escapeXml(notes)}</note>")
            }
            sb.appendLine()
        }

        sb.appendLine("      </div>")
        sb.appendLine("    </body>")
        sb.appendLine("  </text>")
        sb.appendLine("</TEI>")

        return sb.toString()
    }

    fun exportToMarkdown(
        document: DocumentEntity,
        folio: FolioEntity,
        lines: List<LineWithTranscription>
    ): String {
        val sb = StringBuilder()
        sb.appendLine("# ${document.title}")
        sb.appendLine("**Folio:** ${folio.folioNumber} — ${folio.title}")
        sb.appendLine("**Naskah/Repositori:** ${document.repository}")
        sb.appendLine("**Periode/Khat:** ${document.datePeriod} (${document.scriptType})")
        sb.appendLine()
        sb.appendLine("---")
        sb.appendLine()
        sb.appendLine("## Teks Transkripsi Arab")
        sb.appendLine()

        lines.forEach { item ->
            val text = item.transcription?.text?.ifBlank { item.line.originalScriptText } ?: item.line.originalScriptText
            val statusBadge = when (item.transcription?.status) {
                "completed" -> "✓"
                "unclear" -> "❓"
                "annotated" -> "📝"
                else -> "✎"
            }
            sb.appendLine("${item.line.lineNumber}. $statusBadge **${text}**")
            if (item.line.contextTranslation.isNotBlank()) {
                sb.appendLine("   *Terjemah/Konteks:* ${item.line.contextTranslation}")
            }
            if (!item.transcription?.notes.isNullOrBlank()) {
                sb.appendLine("   *Catatan Paleografi:* ${item.transcription?.notes}")
            }
            sb.appendLine()
        }

        return sb.toString()
    }

    private fun escapeXml(str: String): String {
        return str
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
