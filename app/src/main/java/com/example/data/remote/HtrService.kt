package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class HtrRecognitionResult(
    val recognizedText: String,
    val confidence: Float,
    val alternativeReadings: List<String> = emptyList(),
    val notes: String = "",
    val isAiGenerated: Boolean = true
)

class HtrService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    // In-memory cache for line HTR results
    private val memoryCache = mutableMapOf<String, HtrRecognitionResult>()

    suspend fun recognizeArabicLine(
        lineId: String,
        manuscriptTextPrompt: String,
        scriptType: String = "Naskh",
        contextInfo: String = "",
        lineImageBase64: String? = null
    ): HtrRecognitionResult = withContext(Dispatchers.IO) {
        val cacheKey = "htr_${lineId}_${(lineImageBase64 ?: manuscriptTextPrompt).hashCode()}"
        memoryCache[cacheKey]?.let { return@withContext it }

        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY" && lineImageBase64 != null) {
            try {
                // HTR sungguhan: kirim potongan FOTO baris manuskrip ke Gemini Vision,
                // model membaca langsung dari gambar — bukan diberi tahu teksnya duluan.
                val result = callGeminiHtrApiWithImage(apiKey, lineImageBase64, scriptType, contextInfo)
                if (result != null) {
                    memoryCache[cacheKey] = result
                    return@withContext result
                }
            } catch (e: Exception) {
                Log.e("HtrService", "Gemini Vision API call failed, falling back to local engine", e)
            }
        }

        // Tanpa foto folio terlampir (atau panggilan API gagal): TIDAK ADA cara untuk
        // benar-benar mengenali tulisan tangan. Fallback ini hanya menyalin balik teks
        // referensi yang sudah ada di database sebagai draf awal — bukan hasil OCR.
        val localResult = generatePaleographyHtrResult(
            lineText = manuscriptTextPrompt,
            scriptType = scriptType,
            hasImage = lineImageBase64 != null
        )
        memoryCache[cacheKey] = localResult
        return@withContext localResult
    }

    private fun callGeminiHtrApiWithImage(
        apiKey: String,
        imageBase64: String,
        scriptType: String,
        contextInfo: String
    ): HtrRecognitionResult? {
        // TODO(produksi): API key ini masih dipanggil langsung dari client (BuildConfig),
        // sehingga bisa diekstrak dari APK release lewat reverse-engineering. Aman untuk
        // prototipe, tapi sebelum rilis publik pindahkan panggilan Gemini ini ke backend
        // proxy milik sendiri supaya key tidak ikut terbawa ke tangan pengguna.
        // Key dikirim lewat header (bukan query string URL) supaya tidak ikut tercatat
        // kalau ada crash-reporting/interceptor yang mencatat URL request.
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

        val systemPrompt = """
            You are an expert Arabic Paleographer and Handwritten Text Recognition (HTR/ATR) model specializing in historical Arabic manuscripts ($scriptType script).
            You will be given a cropped photograph of a SINGLE LINE from a handwritten Arabic manuscript.
            Read the handwriting directly from the image. Do NOT invent or guess text that is not visually present in the image.
            If part of the line is illegible, mark it clearly in paleographicalNotes rather than fabricating text.
            Return ONLY a valid JSON object with the following fields:
            {
              "transcription": "Precise Arabic text with diacritics exactly as visible in the image",
              "confidence": 0.95,
              "alternativeReadings": ["alternative reading 1", "unpointed rasm variant"],
              "paleographicalNotes": "Brief analytical note on ligatures, ductus, abbreviations, or illegible portions"
            }
        """.trimIndent()

        val promptText = buildString {
            append("Baca dan transkripsikan foto baris manuskrip Arab khat $scriptType ini persis seperti yang terlihat pada gambar.")
            if (contextInfo.isNotBlank()) {
                append(" Konteks tambahan (bukan jawaban, jangan dijadikan acuan teks): $contextInfo")
            }
        }

        val requestJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", promptText))
                        put(JSONObject().apply {
                            put("inlineData", JSONObject().apply {
                                put("mimeType", "image/jpeg")
                                put("data", imageBase64)
                            })
                        })
                    })
                })
            })
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().put("text", systemPrompt))
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
            })
        }

        val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .addHeader("x-goog-api-key", apiKey)
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            Log.w("HtrService", "API returned status: ${response.code}")
            return null
        }

        val responseString = response.body?.string() ?: return null
        val responseJson = JSONObject(responseString)
        val candidates = responseJson.optJSONArray("candidates") ?: return null
        val firstCandidate = candidates.optJSONObject(0) ?: return null
        val content = firstCandidate.optJSONObject("content") ?: return null
        val parts = content.optJSONArray("parts") ?: return null
        val text = parts.optJSONObject(0)?.optString("text") ?: return null

        val parsed = JSONObject(text)
        val transcription = parsed.optString("transcription", "")
        if (transcription.isBlank()) return null // Jangan kembalikan hasil kosong seolah berhasil
        val confidence = parsed.optDouble("confidence", 0.85).toFloat()
        val altArray = parsed.optJSONArray("alternativeReadings")
        val altList = mutableListOf<String>()
        if (altArray != null) {
            for (i in 0 until altArray.length()) {
                altList.add(altArray.getString(i))
            }
        }
        val notes = parsed.optString("paleographicalNotes", "Dibaca langsung dari foto oleh Gemini Vision HTR.")

        return HtrRecognitionResult(
            recognizedText = transcription,
            confidence = confidence,
            alternativeReadings = altList,
            notes = notes,
            isAiGenerated = true
        )
    }

    private fun generatePaleographyHtrResult(
        lineText: String,
        scriptType: String,
        hasImage: Boolean
    ): HtrRecognitionResult {
        // High quality paleography simulation engine
        val cleanedText = lineText.trim()
        val confidence = when {
            cleanedText.contains("۝") || cleanedText.contains("ﷺ") -> 0.98f
            cleanedText.length > 50 -> 0.94f
            else -> 0.91f
        }

        val altReadings = mutableListOf<String>()
        // Generate interesting paleographic variants (e.g. unpointed rasm or variant vocalization)
        if (cleanedText.contains("اللَّهِ")) {
            altReadings.add(cleanedText.replace("اللَّهِ", "الله"))
        }
        if (cleanedText.contains("الرَّحْمَٰنِ")) {
            altReadings.add(cleanedText.replace("الرَّحْمَٰنِ", "الرحمن"))
        }
        if (cleanedText.contains("قَالَ")) {
            altReadings.add(cleanedText.replace("قَالَ", "قال"))
        }

        val statusNote = if (hasImage) {
            "Pemanggilan API Gemini Vision gagal — teks referensi disalin balik sebagai draf sementara (khat $scriptType). Coba lagi saat koneksi tersedia."
        } else {
            "TIDAK ADA FOTO FOLIO TERLAMPIR — ini BUKAN hasil OCR, hanya teks referensi yang disalin balik sebagai draf awal (khat $scriptType). Lampirkan foto folio (ikon kamera di toolbar) untuk transkripsi otomatis sungguhan dari gambar."
        }

        return HtrRecognitionResult(
            recognizedText = cleanedText,
            confidence = if (hasImage) confidence else 0f,
            alternativeReadings = altReadings,
            notes = statusNote,
            isAiGenerated = false
        )
    }
}
