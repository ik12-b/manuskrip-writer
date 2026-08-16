package com.example.utils

data class PaleographyCharItem(
    val char: String,
    val name: String,
    val category: String,
    val description: String = ""
)

object PaleographyCharacters {

    val diacritics = listOf(
        PaleographyCharItem("َ", "Fatḥah", "Harakat", "Vokal pendek a"),
        PaleographyCharItem("ِ", "Kasrah", "Harakat", "Vokal pendek i"),
        PaleographyCharItem("ُ", "Ḍammah", "Harakat", "Vokal pendek u"),
        PaleographyCharItem("ْ", "Sukūn", "Harakat", "Tanda mati/konsonan tanpa vokal"),
        PaleographyCharItem("ّ", "Shaddah", "Harakat", "Tanda tasydid/konsonan ganda"),
        PaleographyCharItem("ً", "Tanwīn Fatḥ", "Tanwin", "Tanwin an (-an)"),
        PaleographyCharItem("ٍ", "Tanwīn Kasr", "Tanwin", "Tanwin in (-in)"),
        PaleographyCharItem("ٌ", "Tanwīn Ḍamm", "Tanwin", "Tanwin un (-un)"),
        PaleographyCharItem("ٰ", "Alif Khonjariyah", "Vokal", "Dagger alif / alif tegak kecil"),
        PaleographyCharItem("ٱ", "Alif Waṣlah", "Vokal", "Hamzatul washl"),
        PaleographyCharItem("ٓ", "Maddah", "Vokal", "Tanda pemanjangan mad")
    )

    val paleographicAndRasm = listOf(
        PaleographyCharItem("ـ", "Taṭwīl / Kashīda", "Paleografi", "Garis pemanjang ligatur kaligrafi"),
        PaleographyCharItem("۝", "Āyah / Fāṣilah", "Tanda Pemisah", "Simbol penanda akhir ayat atau batas pasal"),
        PaleographyCharItem("ء", "Hamzah Munfaridah", "Hamzah", "Hamzah berdiri sendiri"),
        PaleographyCharItem("أ", "Alif Hamzah Atas", "Hamzah", "Hamzah di atas alif"),
        PaleographyCharItem("إ", "Alif Hamzah Bawah", "Hamzah", "Hamzah di bawah alif"),
        PaleographyCharItem("ؤ", "Waw Hamzah", "Hamzah", "Hamzah di atas waw"),
        PaleographyCharItem("ئ", "Ya Hamzah", "Hamzah", "Hamzah di atas ya / nabrah"),
        PaleographyCharItem("ة", "Tāʾ Marbūṭah", "Huruf", "Ta marbutah"),
        PaleographyCharItem("ى", "Alif Maqṣūrah", "Huruf", "Alif bengkok tanpa titik"),
        PaleographyCharItem("ۥ", "Waw Saghirah", "Paleografi", "Waw kecil superskrip"),
        PaleographyCharItem("ۦ", "Ya Saghirah", "Paleografi", "Ya kecil superskrip"),
        PaleographyCharItem("ۚ", "Waqf Jāʾiz", "Waqf", "Tanda waqf boleh berhenti"),
        PaleographyCharItem("ۖ", "Waqf Ṣilā", "Waqf", "Al-washlu awla (lebih utama sambung)"),
        PaleographyCharItem("ۗ", "Waqf Qilā", "Waqf", "Al-waqfu awla (lebih utama berhenti)"),
        PaleographyCharItem("ۘ", "Waqf Lā", "Waqf", "Dilarang berhenti"),
        PaleographyCharItem("ۙ", "Waqf Muṭlaq", "Waqf", "Waqf lazim")
    )

    val editorialApparatus = listOf(
        PaleographyCharItem("⟨ ⟩", "Suplemento", "Kritik Teks", "Teks tambahan rekonstruksi editor"),
        PaleographyCharItem("[ ]", "Lakuna / Hilang", "Kritik Teks", "Bagian teks naskah yang rusak/hilang"),
        PaleographyCharItem("†", "Crux / Korup", "Kritik Teks", "Teks rusak tidak dapat direkonstruksi"),
        PaleographyCharItem("※", "Marginalia", "Catatan", "Tanda rujukan catatan pinggir (hāsyiyah)"),
        PaleographyCharItem("¶", "Paragraf Baru", "Struktur", "Awal babak / paragraf baru manuskrip"),
        PaleographyCharItem("⟨؟⟩", "Dubium / Ragu", "Kritik Teks", "Pembacaan diragukan oleh pentashih"),
        PaleographyCharItem("⟦ ⟧", "Delendo", "Kritik Teks", "Teks yang dicoret oleh penyalin asli (khasf)")
    )

    val allSpecialChars: List<PaleographyCharItem> by lazy {
        diacritics + paleographicAndRasm + editorialApparatus
    }
}
