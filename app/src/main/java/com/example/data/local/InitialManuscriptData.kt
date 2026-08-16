package com.example.data.local

import com.example.data.model.DocumentEntity
import com.example.data.model.FolioEntity
import com.example.data.model.LineEntity
import com.example.data.model.TranscriptionEntity

object InitialManuscriptData {

    val sampleDocuments = listOf(
        DocumentEntity(
            id = "doc_ibn_sina_qanun",
            title = "Al-Qānūn fī al-Ṭibb (Ibn Sīnā)",
            repository = "Süleymaniye Kütüphanesi, Istanbul (MS Ayasofya 3686)",
            datePeriod = "741 H / 1340 M (Abad ke-14)",
            language = "Arabic (العربية)",
            scriptType = "Naskh Ilmi (نسخ علمي)",
            totalFolios = 2,
            description = "Manuskrip kedokteran klasik karya Ibnu Sina mengenai traktat keseimbangan cairan tubuh (Mizāj & Akhlāṭ) dan diagnosa denyut nadi."
        ),
        DocumentEntity(
            id = "doc_sahih_bukhari",
            title = "Al-Jāmiʿ al-Ṣaḥīḥ (Al-Bukhārī)",
            repository = "Bibliothèque nationale de France (Arabe 6059)",
            datePeriod = "698 H / 1299 M",
            language = "Arabic (العربية)",
            scriptType = "Naskh Andalusi-Mamluk",
            totalFolios = 2,
            description = "Salinan historis kitab hadits Sahih Bukhari dengan tanda qira'ah (sama') dan sanad periwayatan Al-Firabri."
        ),
        DocumentEntity(
            id = "doc_kufic_quran",
            title = "Fragment Mushaf Kufi Kuno",
            repository = "Museum Kesenian Islam Kairo (No. Inv 148)",
            datePeriod = "Abad ke-3 H / Abad ke-9 M",
            language = "Arabic (العربية)",
            scriptType = "Kufi Abbasid Klasik (كوفي قديم)",
            totalFolios = 1,
            description = "Fragmen perkamen perkamen kulit rusa dengan kaligrafi Kufi awal tanpa titik i'jam modern, berhias tanda fashl emas."
        ),
        DocumentEntity(
            id = "doc_kitab_al_manazir",
            title = "Kitāb al-Manāẓir (Ibn al-Haytham)",
            repository = "Fatih Manuscript Library, Istanbul (MS 3212)",
            datePeriod = "646 H / 1248 M",
            language = "Arabic (العربية)",
            scriptType = "Naskh Ilmi",
            totalFolios = 1,
            description = "Risalah optika geometri dan teori pembiasan cahaya oleh Abu Ali al-Hasan Ibn al-Haytham."
        ),
        DocumentEntity(
            id = "doc_dummy_pdf_test",
            title = "📄 Dummy Test PDF: Matan Al-Ajurrumiyyah (Uji Coba)",
            repository = "Koleksi Uji Coba Filologi & Ekspor PDF ManuScribe",
            datePeriod = "Salinan Digital Komparasi 1446 H / 2025 M",
            language = "Arabic (العربية)",
            scriptType = "Naskh Klasik Terstandarisasi",
            totalFolios = 2,
            description = "Naskah dummy untuk pengujian fitur ekspor PDF bersinkronisasi, perataan auto-align, dan simulasi pengetikan mesin ketik."
        )
    )

    val sampleFolios = listOf(
        // Ibn Sina folios
        FolioEntity(
            id = "folio_ibnsina_12r",
            documentId = "doc_ibn_sina_qanun",
            folioNumber = "12r",
            title = "Folio 12r: Fī Bayān al-Amrāḍ wa-l-Aʿrāḍ",
            totalLines = 8
        ),
        FolioEntity(
            id = "folio_ibnsina_12v",
            documentId = "doc_ibn_sina_qanun",
            folioNumber = "12v",
            title = "Folio 12v: Fī Taʿrīf al-Nabḍ wa Aqnāsihi",
            totalLines = 6
        ),
        // Bukhari folios
        FolioEntity(
            id = "folio_bukhari_1a",
            documentId = "doc_sahih_bukhari",
            folioNumber = "1a",
            title = "Folio 1a: Bāb Badʾ al-Waḥy wa-l-Niyyāt",
            totalLines = 7
        ),
        FolioEntity(
            id = "folio_bukhari_1b",
            documentId = "doc_sahih_bukhari",
            folioNumber = "1b",
            title = "Folio 1b: Ḥadīth ʿAlqamah ʿan ʿUmar",
            totalLines = 6
        ),
        // Kufic folio
        FolioEntity(
            id = "folio_kufic_4r",
            documentId = "doc_kufic_quran",
            folioNumber = "4r",
            title = "Folio 4r: Ṣūrat al-Fatḥ (Rasm Kufi Kuno)",
            totalLines = 6
        ),
        // Ibn al-Haytham folio
        FolioEntity(
            id = "folio_manazir_8r",
            documentId = "doc_kitab_al_manazir",
            folioNumber = "8r",
            title = "Folio 8r: Kayfiyyat Wurūd al-Ḍawʾ ilā al-Baṣar",
            totalLines = 6
        ),
        // Dummy PDF Test Folios
        FolioEntity(
            id = "folio_dummy_pdf_1a",
            documentId = "doc_dummy_pdf_test",
            folioNumber = "1a",
            title = "Folio 1a: Bāb Kalām wa Aqsāmuh (Uji Coba Dummy)",
            totalLines = 8
        ),
        FolioEntity(
            id = "folio_dummy_pdf_1b",
            documentId = "doc_dummy_pdf_test",
            folioNumber = "1b",
            title = "Folio 1b: Bāb ʿAlāmāt al-Iʿrāb (Uji Coba Dummy)",
            totalLines = 7
        )
    )

    val sampleLines = listOf(
        // --- Folio Ibn Sina 12r ---
        LineEntity(
            id = "line_ibnsina_12r_1",
            folioId = "folio_ibnsina_12r",
            lineNumber = 1,
            originalScriptText = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ ۝ قَالَ الشَّيْخُ الرَّئِيسُ أَبُو عَلِيٍّ بْنُ سِينَا رَحِمَهُ اللَّهُ",
            contextTranslation = "Dengan nama Allah Yang Maha Pengasih lagi Maha Penyayang. Berkata Asy-Syaikh Ar-Ra'is Abu Ali bin Sina semoga Allah merahmatinya.",
            scriptStyle = "Naskh Ilmi",
            bboxTop = 0.05f, bboxLeft = 0.05f, bboxWidth = 0.90f, bboxHeight = 0.10f
        ),
        LineEntity(
            id = "line_ibnsina_12r_2",
            folioId = "folio_ibnsina_12r",
            lineNumber = 2,
            originalScriptText = "الْحَمْدُ لِلَّهِ حَمْدَ الشَّاكِرِينَ، وَالصَّلَاةُ عَلَى نَبِيِّهِ مُحَمَّدٍ وَآلِهِ أَجْمَعِينَ.",
            contextTranslation = "Segala puji bagi Allah dengan pujian orang-orang yang bersyukur, dan sholawat atas Nabi-Nya Muhammad beserta keluarganya.",
            scriptStyle = "Naskh Ilmi",
            bboxTop = 0.16f, bboxLeft = 0.05f, bboxWidth = 0.90f, bboxHeight = 0.10f
        ),
        LineEntity(
            id = "line_ibnsina_12r_3",
            folioId = "folio_ibnsina_12r",
            lineNumber = 3,
            originalScriptText = "إِنَّ الطِّبَّ عِلْمٌ يُتَعَرَّفُ مِنْهُ أَحْوَالُ بَدَنِ الْإِنْسَانِ مِنْ جِهَةِ مَا يَصِحُّ وَيَزُولُ عَنِ الصِّحَّةِ",
            contextTranslation = "Sesungguhnya ilmu kedokteran adalah sains untuk mengetahui keadaan tubuh manusia dari sisi kesehatan dan perubahan sakit.",
            scriptStyle = "Naskh Ilmi",
            bboxTop = 0.27f, bboxLeft = 0.05f, bboxWidth = 0.90f, bboxHeight = 0.10f
        ),
        LineEntity(
            id = "line_ibnsina_12r_4",
            folioId = "folio_ibnsina_12r",
            lineNumber = 4,
            originalScriptText = "لِيَحْفَظَ الصِّحَّةَ حَاصِلَةً وَيَسْتَرِدَّهَا زَائِلَةً، وَالْأَرْكَانُ هِيَ الْأُسْطُقُسَّاتُ الْأُولَى",
            contextTranslation = "Untuk menjaga kesehatan yang telah ada dan memulihkannya bila hilang, dan rukun-rukun adalah unsur-unsur materi pertama.",
            scriptStyle = "Naskh Ilmi",
            bboxTop = 0.38f, bboxLeft = 0.05f, bboxWidth = 0.90f, bboxHeight = 0.10f
        ),
        LineEntity(
            id = "line_ibnsina_12r_5",
            folioId = "folio_ibnsina_12r",
            lineNumber = 5,
            originalScriptText = "وَهِيَ النَّارُ وَالْهَوَاءُ وَالْمَاءُ وَالْأَرْضُ، بِامْتِزَاجِهَا تَحْدُثُ الْأَمْزِجَةُ الْمُتَفَاوِتَةُ",
            contextTranslation = "Yaitu api, udara, air, dan tanah, yang dengan percampurannya terbentuklah temperamen-temperamen (mizaj) yang berbeda-beda.",
            scriptStyle = "Naskh Ilmi",
            bboxTop = 0.49f, bboxLeft = 0.05f, bboxWidth = 0.90f, bboxHeight = 0.10f
        ),
        LineEntity(
            id = "line_ibnsina_12r_6",
            folioId = "folio_ibnsina_12r",
            lineNumber = 6,
            originalScriptText = "وَالْأَخْلَاطُ أَرْبَعَةٌ: الدَّمُ وَالْبَلْغَمُ وَالصَّفْرَاءُ وَالسَّوْدَاءُ، وَلِكُلِّ خِلْطٍ طَبِيعَةٌ",
            contextTranslation = "Dan cairan tubuh (al-akhlat) ada empat: darah, dahak/flegma, empedu kuning, dan empedu hitam, dan tiap cairan memiliki sifat alami.",
            scriptStyle = "Naskh Ilmi",
            bboxTop = 0.60f, bboxLeft = 0.05f, bboxWidth = 0.90f, bboxHeight = 0.10f
        ),
        LineEntity(
            id = "line_ibnsina_12r_7",
            folioId = "folio_ibnsina_12r",
            lineNumber = 7,
            originalScriptText = "فَالدَّمُ حَارٌّ رَطْبٌ، وَالصَّفْرَاءُ حَارَّةٌ يَابِسَةٌ، وَالسَّوْدَاءُ بَارِدَةٌ يَابِسَةٌ",
            contextTranslation = "Maka darah bersuhu panas lembab, empedu kuning panas kering, dan empedu hitam dingin kering.",
            scriptStyle = "Naskh Ilmi",
            bboxTop = 0.71f, bboxLeft = 0.05f, bboxWidth = 0.90f, bboxHeight = 0.10f
        ),
        LineEntity(
            id = "line_ibnsina_12r_8",
            folioId = "folio_ibnsina_12r",
            lineNumber = 8,
            originalScriptText = "وَالْبَلْغَمُ بَارِدٌ رَطْبٌ، فَمَتَى اعْتَدَلَتْ سَلِمَ الْبَدَنُ وَمَتَى غَلَبَ أَحَدُهَا وَقَعَ السَّقَمُ.",
            contextTranslation = "Dan dahak dingin lembab; bila seimbang maka selamatlah tubuh, dan bila salah satunya mendominasi maka timbullah penyakit.",
            scriptStyle = "Naskh Ilmi",
            bboxTop = 0.82f, bboxLeft = 0.05f, bboxWidth = 0.90f, bboxHeight = 0.10f
        ),

        // --- Folio Ibn Sina 12v ---
        LineEntity(
            id = "line_ibnsina_12v_1",
            folioId = "folio_ibnsina_12v",
            lineNumber = 1,
            originalScriptText = "فَصْلٌ فِي مَعْرِفَةِ النَّبْضِ: النَّبْضُ حَرَكَةٌ فِي الْعُرُوقِ وَالْأَوْرِدَةِ الضَّارِبَةِ",
            contextTranslation = "Pasal tentang mengenal denyut nadi: Nadi adalah gerakan pada pembuluh darah arteri yang berdenyut.",
            scriptStyle = "Naskh Ilmi",
            bboxTop = 0.05f, bboxLeft = 0.05f, bboxWidth = 0.90f, bboxHeight = 0.12f
        ),
        LineEntity(
            id = "line_ibnsina_12v_2",
            folioId = "folio_ibnsina_12v",
            lineNumber = 2,
            originalScriptText = "مُؤَلَّفَةٌ مِنْ قَبْضٍ وَبَسْطٍ لِتَرْوِيحِ الْقَلْبِ بِالنَّسِيمِ وَإِخْرَاجِ الْفُضُولِ الدُّخَانِيَّةِ",
            contextTranslation = "Tersusun atas kontraksi (qabdh) dan ekspansi (basth) untuk menyegarkan jantung dengan udara segar dan mengeluarkan uap sisa.",
            scriptStyle = "Naskh Ilmi",
            bboxTop = 0.18f, bboxLeft = 0.05f, bboxWidth = 0.90f, bboxHeight = 0.12f
        ),
        LineEntity(
            id = "line_ibnsina_12v_3",
            folioId = "folio_ibnsina_12v",
            lineNumber = 3,
            originalScriptText = "وَأَجْنَاسُ النَّبْضِ تُرَى فِي عَشَرَةِ أَوْجُهٍ: فِي مِقْدَارِهِ، وَقُوَّةِ ضَرْبَتِهِ، وَسُرْعَتِهِ",
            contextTranslation = "Dan jenis-jenis denyut nadi diamati melalui 10 segi: ukurannya, kekuatan pukulannya, kecepatannya...",
            scriptStyle = "Naskh Ilmi",
            bboxTop = 0.31f, bboxLeft = 0.05f, bboxWidth = 0.90f, bboxHeight = 0.12f
        ),
        LineEntity(
            id = "line_ibnsina_12v_4",
            folioId = "folio_ibnsina_12v",
            lineNumber = 4,
            originalScriptText = "وَتَوَاتُرِهِ، وَامْتِلَائِهِ، وَمَلْمَسِهِ مِنَ الصَّلَابَةِ وَاللِّينِ، وَاسْتِوَائِهِ وَاخْتِلَافِهِ",
            contextTranslation = "Frekuensinya, kepenuhannya, tekstur rabaannya dari kekerasan atau kelenturan, serta keteraturan dan anomali ritmenya.",
            scriptStyle = "Naskh Ilmi",
            bboxTop = 0.44f, bboxLeft = 0.05f, bboxWidth = 0.90f, bboxHeight = 0.12f
        ),
        LineEntity(
            id = "line_ibnsina_12v_5",
            folioId = "folio_ibnsina_12v",
            lineNumber = 5,
            originalScriptText = "فَالنَّبْضُ الْعَظِيمُ يَدُلُّ عَلَى كَمَالِ الْقُوَّةِ وَحَاجَةِ الْحَرَارَةِ الْغَرِيزِيَّةِ إِلَى التَّرْوِيحِ",
            contextTranslation = "Maka nadi yang besar menunjukkan kesempurnaan kekuatan vital dan perlunya panas alami tubuh untuk disegarkan.",
            scriptStyle = "Naskh Ilmi",
            bboxTop = 0.57f, bboxLeft = 0.05f, bboxWidth = 0.90f, bboxHeight = 0.12f
        ),
        LineEntity(
            id = "line_ibnsina_12v_6",
            folioId = "folio_ibnsina_12v",
            lineNumber = 6,
            originalScriptText = "وَالصَّغِيرُ يَدُلُّ عَلَى ضَعْفِ الْقُوَّةِ أَوِ انْحِطَاطِ الْمَادَّةِ، وَاللَّهُ أَعْلَمُ بِالصَّوَابِ.",
            contextTranslation = "Dan denyut yang kecil menunjukkan kelemahan energi tubuh atau penurunan substansi materi, dan Allah lebih mengetahui kebenaran.",
            scriptStyle = "Naskh Ilmi",
            bboxTop = 0.70f, bboxLeft = 0.05f, bboxWidth = 0.90f, bboxHeight = 0.12f
        ),

        // --- Folio Sahih Bukhari 1a ---
        LineEntity(
            id = "line_bukhari_1a_1",
            folioId = "folio_bukhari_1a",
            lineNumber = 1,
            originalScriptText = "كِتَابُ بَدْءِ الْوَحْيِ ۝ بَابُ كَيْفَ كَانَ بَدْءُ الْوَحْيِ إِلَى رَسُولِ اللَّهِ ﷺ",
            contextTranslation = "Kitab Permulaan Wahyu. Bab bagaimana permulaan wahyu kepada Rasulullah ﷺ.",
            scriptStyle = "Naskh Andalusi-Mamluk",
            bboxTop = 0.05f, bboxLeft = 0.05f, bboxWidth = 0.90f, bboxHeight = 0.11f
        ),
        LineEntity(
            id = "line_bukhari_1a_2",
            folioId = "folio_bukhari_1a",
            lineNumber = 2,
            originalScriptText = "حَدَّثَنَا الْحُمَيْدِيُّ عَبْدُ اللَّهِ بْنُ الزُّبَيْرِ قَالَ: حَدَّثَنَا سُفْيَانُ قَالَ: حَدَّثَنَا يَحْيَى بْنُ سَعِيدٍ الْأَنْصَارِيُّ",
            contextTranslation = "Menceritakan kepada kami Al-Humaidi Abdullah bin Az-Zubair berkata: Menceritakan kepada kami Sufyan berkata: Menceritakan kepada kami Yahya bin Sa'id Al-Ansari.",
            scriptStyle = "Naskh Andalusi-Mamluk",
            bboxTop = 0.17f, bboxLeft = 0.05f, bboxWidth = 0.90f, bboxHeight = 0.11f
        ),
        LineEntity(
            id = "line_bukhari_1a_3",
            folioId = "folio_bukhari_1a",
            lineNumber = 3,
            originalScriptText = "أَخْبَرَنِي مُحَمَّدُ بْنُ إِبْرَاهِيمَ التَّيْمِيُّ أَنَّهُ سَمِعَ عَلْقَمَةَ بْنَ وَقَّاصٍ اللَّيْثِيَّ يَقُولُ:",
            contextTranslation = "Mengabarkan kepadaku Muhammad bin Ibrahim At-Taimi bahwasanya ia mendengar 'Alqamah bin Waqqas Al-Laitsi berkata:",
            scriptStyle = "Naskh Andalusi-Mamluk",
            bboxTop = 0.29f, bboxLeft = 0.05f, bboxWidth = 0.90f, bboxHeight = 0.11f
        ),
        LineEntity(
            id = "line_bukhari_1a_4",
            folioId = "folio_bukhari_1a",
            lineNumber = 4,
            originalScriptText = "سَمِعْتُ عُمَرَ بْنَ الْخَطَّابِ رَضِيَ اللَّهُ عَنْهُ عَلَى الْمِنْبَرِ قَالَ: سَمِعْتُ رَسُولَ اللَّهِ ﷺ يَقُولُ:",
            contextTranslation = "Aku mendengar Umar bin Al-Khattab radhiyallahu 'anhu di atas mimbar berkata: Aku mendengar Rasulullah ﷺ bersabda:",
            scriptStyle = "Naskh Andalusi-Mamluk",
            bboxTop = 0.41f, bboxLeft = 0.05f, bboxWidth = 0.90f, bboxHeight = 0.11f
        ),
        LineEntity(
            id = "line_bukhari_1a_5",
            folioId = "folio_bukhari_1a",
            lineNumber = 5,
            originalScriptText = "«إِنَّمَا الْأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى، فَمَنْ كَانَتْ هِجْرَتُهُ إِلَى دُنْيَا يُصِيبُهَا",
            contextTranslation = "'Sesungguhnya setiap amalan bergantung pada niatnya, dan setiap orang memperoleh apa yang diniatkannya; maka barangsiapa hijrahnya karena dunia yang ingin diraihnya...'",
            scriptStyle = "Naskh Andalusi-Mamluk",
            bboxTop = 0.53f, bboxLeft = 0.05f, bboxWidth = 0.90f, bboxHeight = 0.11f
        ),
        LineEntity(
            id = "line_bukhari_1a_6",
            folioId = "folio_bukhari_1a",
            lineNumber = 6,
            originalScriptText = "أَوْ إِلَى امْرَأَةٍ يَنْكِحُهَا فَهِجْرَتُهُ إِلَى مَا هَاجَرَ إِلَيْهِ».",
            contextTranslation = "'Atau wanita yang ingin dinikahinya, maka hijrahnya kepada apa yang ia tuju dalam hijrahnya.'",
            scriptStyle = "Naskh Andalusi-Mamluk",
            bboxTop = 0.65f, bboxLeft = 0.05f, bboxWidth = 0.90f, bboxHeight = 0.11f
        ),
        LineEntity(
            id = "line_bukhari_1a_7",
            folioId = "folio_bukhari_1a",
            lineNumber = 7,
            originalScriptText = "[عَلَيْهِ عَلَامَةُ الصِّحَّةِ وَالْمُقَابَلَةِ بِأَصْلِ الشَّيْخِ رَحِمَهُ اللَّهُ ۝]",
            contextTranslation = "[Catatan marjinal: Tertulis tanda tashih dan muqabalah pencocokan dengan naskah asli Syaikh rahmatullah 'alaih].",
            scriptStyle = "Naskh Andalusi-Mamluk",
            bboxTop = 0.77f, bboxLeft = 0.05f, bboxWidth = 0.90f, bboxHeight = 0.11f
        ),

        // --- Folio Kufic Quran 4r ---
        LineEntity(
            id = "line_kufic_4r_1",
            folioId = "folio_kufic_4r",
            lineNumber = 1,
            originalScriptText = "بسم ٱلله ٱلرحمں ٱلرحىم ۝ اںا فتحںا لك فتحا مىىںا",
            contextTranslation = "Rasm Kufi Kuno: Bismillahir-Rahmanir-Rahim. Inna fatahna laka fathan mubina.",
            scriptStyle = "Kufi Abbasid Klasik",
            bboxTop = 0.06f, bboxLeft = 0.05f, bboxWidth = 0.90f, bboxHeight = 0.13f
        ),
        LineEntity(
            id = "line_kufic_4r_2",
            folioId = "folio_kufic_4r",
            lineNumber = 2,
            originalScriptText = "لىعفر لك ٱلله ما تقدم مں ذںبك وما تاخر",
            contextTranslation = "Rasm Kufi Kuno: Li-yaghfira lakallahu ma taqaddama min dhanbika wa ma ta'akhkhara.",
            scriptStyle = "Kufi Abbasid Klasik",
            bboxTop = 0.20f, bboxLeft = 0.05f, bboxWidth = 0.90f, bboxHeight = 0.13f
        ),
        LineEntity(
            id = "line_kufic_4r_3",
            folioId = "folio_kufic_4r",
            lineNumber = 3,
            originalScriptText = "وىتم ںعمته علىك وىهدىك صرطا مسىقىما ۝",
            contextTranslation = "Rasm Kufi Kuno: Wa yutimma ni'matahu 'alayka wa yahdiyaka siratan mustaqima.",
            scriptStyle = "Kufi Abbasid Klasik",
            bboxTop = 0.34f, bboxLeft = 0.05f, bboxWidth = 0.90f, bboxHeight = 0.13f
        ),
        LineEntity(
            id = "line_kufic_4r_4",
            folioId = "folio_kufic_4r",
            lineNumber = 4,
            originalScriptText = "وىںصرك ٱلله ںصرا عزىزا ۝ هو ٱلذى اںزل",
            contextTranslation = "Rasm Kufi Kuno: Wa yansurakallahu nasran 'aziza. Huwal-ladhi anzala...",
            scriptStyle = "Kufi Abbasid Klasik",
            bboxTop = 0.48f, bboxLeft = 0.05f, bboxWidth = 0.90f, bboxHeight = 0.13f
        ),
        LineEntity(
            id = "line_kufic_4r_5",
            folioId = "folio_kufic_4r",
            lineNumber = 5,
            originalScriptText = "ٱلسكىںة فى قلوٮ ٱلمومںىں لىزدادوا اىماںا",
            contextTranslation = "Rasm Kufi Kuno: As-sakinata fi qulubil-mu'minina li-yazdadu imanan...",
            scriptStyle = "Kufi Abbasid Klasik",
            bboxTop = 0.62f, bboxLeft = 0.05f, bboxWidth = 0.90f, bboxHeight = 0.13f
        ),
        LineEntity(
            id = "line_kufic_4r_6",
            folioId = "folio_kufic_4r",
            lineNumber = 6,
            originalScriptText = "مع اىماںهم ولله حںود ٱلسموت وٱلارض ۝",
            contextTranslation = "Rasm Kufi Kuno: Ma'a imanihim wa lillahi junudus-samawati wal-ard.",
            scriptStyle = "Kufi Abbasid Klasik",
            bboxTop = 0.76f, bboxLeft = 0.05f, bboxWidth = 0.90f, bboxHeight = 0.13f
        ),

        // --- Folio Kitab al-Manazir 8r ---
        LineEntity(
            id = "line_manazir_8r_1",
            folioId = "folio_manazir_8r",
            lineNumber = 1,
            originalScriptText = "الْمَقَالَةُ الْأُولَى: فِي كَيْفِيَّةِ الْإِبْصَارِ بِالْجُمْلَةِ وَخَوَاصِّ الضَّوْءِ",
            contextTranslation = "Risalah Pertama: Mengenai tata cara penglihatan secara umum dan sifat-sifat khusus cahaya.",
            scriptStyle = "Naskh Ilmi",
            bboxTop = 0.05f, bboxLeft = 0.05f, bboxWidth = 0.90f, bboxHeight = 0.12f
        ),
        LineEntity(
            id = "line_manazir_8r_2",
            folioId = "folio_manazir_8r",
            lineNumber = 2,
            originalScriptText = "نَجِدُ كُلَّ ضَوْءٍ يَصْدُرُ عَنْ كُلِّ جِسْمٍ مُضِيءٍ بِذَاتِهِ كَالشَّمْسِ وَالنَّارِ",
            contextTranslation = "Kita mendapati setiap cahaya memancar dari setiap benda yang bercahaya dengan sendirinya seperti matahari dan api.",
            scriptStyle = "Naskh Ilmi",
            bboxTop = 0.18f, bboxLeft = 0.05f, bboxWidth = 0.90f, bboxHeight = 0.12f
        ),
        LineEntity(
            id = "line_manazir_8r_3",
            folioId = "folio_manazir_8r",
            lineNumber = 3,
            originalScriptText = "يَمْتَدُّ عَلَى خُطُوطٍ مُسْتَقِيمَةٍ فِي الْأَجْسَامِ الْمُشِفَّةِ فِي جَمِيعِ الْجِهَاتِ",
            contextTranslation = "Merambat lurus melalui garis-garis lurus di dalam medium yang tembus pandang (transparan) ke segala arah.",
            scriptStyle = "Naskh Ilmi",
            bboxTop = 0.31f, bboxLeft = 0.05f, bboxWidth = 0.90f, bboxHeight = 0.12f
        ),
        LineEntity(
            id = "line_manazir_8r_4",
            folioId = "folio_manazir_8r",
            lineNumber = 4,
            originalScriptText = "وَإِذَا وَقَعَ عَلَى جِسْمٍ كَثِيفٍ صَقِيلٍ انْعَكَسَ بِزَاوِيَةٍ مُسَاوِيَةٍ لِزَاوِيَةِ الْوُقُوعِ",
            contextTranslation = "Dan apabila cahaya jatuh pada benda pejal yang mengkilap, ia dipantulkan dengan sudut pantul yang sama dengan sudut datang.",
            scriptStyle = "Naskh Ilmi",
            bboxTop = 0.44f, bboxLeft = 0.05f, bboxWidth = 0.90f, bboxHeight = 0.12f
        ),
        LineEntity(
            id = "line_manazir_8r_5",
            folioId = "folio_manazir_8r",
            lineNumber = 5,
            originalScriptText = "وَالْبَصَرُ لَا يُدْرِكُ الْمُبْصَرَاتِ إِلَّا بِوُرُودِ الضَّوْءِ مِنْهَا إِلَى الْعَيْنِ لَا بِشُعَاعٍ يَخْرُجُ مِنْهَا",
            contextTranslation = "Dan mata tidak mempersepsi objek-objek melainkan karena masuknya cahaya dari objek ke mata, bukan dari sinar yang keluar dari mata.",
            scriptStyle = "Naskh Ilmi",
            bboxTop = 0.57f, bboxLeft = 0.05f, bboxWidth = 0.90f, bboxHeight = 0.12f
        ),
        LineEntity(
            id = "line_manazir_8r_6",
            folioId = "folio_manazir_8r",
            lineNumber = 6,
            originalScriptText = "كَمَا كَانَ يَزْعُمُ الْأَوَائِلُ، وَهَذَا بُرْهَانٌ حِسِّيٌّ قَاطِعٌ قَائِمٌ عَلَى الِاعْتِبَارِ.",
            contextTranslation = "Sebagaimana diduga oleh para filsuf terdahulu, dan ini merupakan bukti empiris yang pasti berdasar eksperimen saintifik.",
            scriptStyle = "Naskh Ilmi",
            bboxTop = 0.70f, bboxLeft = 0.05f, bboxWidth = 0.90f, bboxHeight = 0.12f
        ),
        // --- Dummy PDF Test Folio 1a Lines ---
        LineEntity(
            id = "line_dummy_pdf_1a_1",
            folioId = "folio_dummy_pdf_1a",
            lineNumber = 1,
            originalScriptText = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ ۝ الْكَلَامُ هُوَ اللَّفْظُ الْمُرَكَّبُ الْمُفِيدُ بِالْوَضْعِ",
            contextTranslation = "Dengan nama Allah Yang Maha Pengasih lagi Maha Penyayang. Al-Kalam adalah lafadz yang tersusun yang memberi faedah dengan sengaja.",
            scriptStyle = "Naskh Klasik",
            bboxTop = 0.05f, bboxLeft = 0.05f, bboxWidth = 0.90f, bboxHeight = 0.10f
        ),
        LineEntity(
            id = "line_dummy_pdf_1a_2",
            folioId = "folio_dummy_pdf_1a",
            lineNumber = 2,
            originalScriptText = "وَأَقْسَامُهُ ثَلَاثَةٌ: اسْمٌ، وَفِعْلٌ، وَحَرْفٌ جَاءَ لِمَعْنًى.",
            contextTranslation = "Dan pembagiannya ada tiga: Isim, Fi'il, dan Huruf yang datang untuk membawa makna.",
            scriptStyle = "Naskh Klasik",
            bboxTop = 0.16f, bboxLeft = 0.05f, bboxWidth = 0.90f, bboxHeight = 0.10f
        ),
        LineEntity(
            id = "line_dummy_pdf_1a_3",
            folioId = "folio_dummy_pdf_1a",
            lineNumber = 3,
            originalScriptText = "فَالِاسْمُ يُعْرَفُ بِالْخَفْضِ، وَالتَّنْوِينِ، وَدُخُولِ الْأَلِفِ وَاللَّامِ",
            contextTranslation = "Maka isim dikenal dengan khafadh (jar), tanwin, dan masuknya alif lam (al-).",
            scriptStyle = "Naskh Klasik",
            bboxTop = 0.27f, bboxLeft = 0.05f, bboxWidth = 0.90f, bboxHeight = 0.10f
        ),
        LineEntity(
            id = "line_dummy_pdf_1a_4",
            folioId = "folio_dummy_pdf_1a",
            lineNumber = 4,
            originalScriptText = "وَحُرُوفِ الْخَفْضِ وَهِيَ: مِنْ، وَإِلَى، وَعَنْ، وَعَلَى، وَفِي، وَرُبَّ، وَالْبَاءُ، وَالْكَافُ، وَاللَّامُ",
            contextTranslation = "Dan huruf-huruf khafadh yaitu: min, ila, 'an, 'ala, fi, rubba, ba, kaf, dan lam.",
            scriptStyle = "Naskh Klasik",
            bboxTop = 0.38f, bboxLeft = 0.05f, bboxWidth = 0.90f, bboxHeight = 0.10f
        ),
        LineEntity(
            id = "line_dummy_pdf_1a_5",
            folioId = "folio_dummy_pdf_1a",
            lineNumber = 5,
            originalScriptText = "وَالْفِعْلُ يُعْرَفُ بِقَدْ، وَالسِّينِ، وَسَوْفَ، وَتَاءِ التَّأْنِيثِ السَّاكِنَةِ",
            contextTranslation = "Dan fi'il dikenal dengan qad, sin, saufa, dan ta' ta'nits as-sakinah.",
            scriptStyle = "Naskh Klasik",
            bboxTop = 0.49f, bboxLeft = 0.05f, bboxWidth = 0.90f, bboxHeight = 0.10f
        ),
        LineEntity(
            id = "line_dummy_pdf_1a_6",
            folioId = "folio_dummy_pdf_1a",
            lineNumber = 6,
            originalScriptText = "وَالْحَرْفُ مَا لَا يَصْلُحُ مَعَهُ دَلِيلُ الِاسْمِ وَلَا دَلِيلُ الْفِعْلِ.",
            contextTranslation = "Dan huruf adalah apa yang tidak layak disertai tanda isim maupun tanda fi'il.",
            scriptStyle = "Naskh Klasik",
            bboxTop = 0.60f, bboxLeft = 0.05f, bboxWidth = 0.90f, bboxHeight = 0.10f
        ),
        LineEntity(
            id = "line_dummy_pdf_1a_7",
            folioId = "folio_dummy_pdf_1a",
            lineNumber = 7,
            originalScriptText = "بَابُ مَعْرِفَةِ عَلَامَاتِ الْإِعْرَابِ ۝ لِلرَّفْعِ أَرْبَعُ عَلَامَاتٍ: الضَّمَّةُ، وَالْوَاوُ، وَالْأَلِفُ، وَالنُّونُ",
            contextTranslation = "Bab Mengenal Tanda-Tanda I'rab. Bagi rafa' ada 4 tanda: dhammah, wawu, alif, dan nun.",
            scriptStyle = "Naskh Klasik",
            bboxTop = 0.71f, bboxLeft = 0.05f, bboxWidth = 0.90f, bboxHeight = 0.10f
        ),
        LineEntity(
            id = "line_dummy_pdf_1a_8",
            folioId = "folio_dummy_pdf_1a",
            lineNumber = 8,
            originalScriptText = "فَأَمَّا الضَّمَّةُ فَتَكُونُ عَلَامَةً لِلرَّفْعِ فِي أَرْبَعَةِ مَوَاضِعَ فِي الِاسْمِ الْمُفْرَدِ وَجَمْعِ التَّكْسِيرِ.",
            contextTranslation = "Adapun dhammah menjadi tanda bagi rafa' pada 4 tempat: isim mufrad dan jama' taksir.",
            scriptStyle = "Naskh Klasik",
            bboxTop = 0.82f, bboxLeft = 0.05f, bboxWidth = 0.90f, bboxHeight = 0.10f
        )
    )

    val sampleTranscriptions = listOf(
        TranscriptionEntity(
            lineId = "line_dummy_pdf_1a_1",
            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ ۝ الْكَلَامُ هُوَ اللَّفْظُ الْمُرَكَّبُ الْمُفِيدُ بِالْوَضْعِ",
            status = "completed",
            notes = "Uji coba baris pertama: Basmalah Thuluth dan ta'rif Kalam disahkan.",
            confidence = 0.99f,
            alternativeReadings = "الْكَلَامُ هُوَ اللَّفْظُ الْمُفِيدُ",
            annotator = "Tester PDF ManuScribe",
            syncedAt = System.currentTimeMillis()
        ),
        TranscriptionEntity(
            lineId = "line_dummy_pdf_1a_2",
            text = "وَأَقْسَامُهُ ثَلَاثَةٌ: اسْمٌ، وَفِعْلٌ، وَحَرْفٌ جَاءَ لِمَعْنًى.",
            status = "completed",
            notes = "Tiga klasifikasi kalimat gramatika Arab.",
            confidence = 0.97f,
            alternativeReadings = "وَأَقْسَامُ الْكَلَامِ ثَلَاثَةٌ",
            annotator = "Tester PDF ManuScribe",
            syncedAt = System.currentTimeMillis()
        ),
        TranscriptionEntity(
            lineId = "line_dummy_pdf_1a_3",
            text = "فَالِاسْمُ يُعْرَفُ بِالْخَفْضِ، وَالتَّنْوِينِ، وَدُخُولِ الْأَلِفِ وَاللَّامِ",
            status = "draft",
            notes = "Tanda pengenal isim: jar, tanwin, dan alif lam.",
            confidence = 0.94f,
            alternativeReadings = "فَالِاسْمُ يُعْرَفُ بِالْجَرِّ",
            annotator = "Tester PDF ManuScribe",
            syncedAt = null
        ),
        TranscriptionEntity(
            lineId = "line_dummy_pdf_1a_4",
            text = "وَحُرُوفِ الْخَفْضِ وَهِيَ: مِنْ، وَإِلَى، وَعَنْ، وَعَلَى، وَفِي، وَرُبَّ، وَالْبَاءُ، وَالْكَافُ، وَاللَّامُ",
            status = "completed",
            notes = "Daftar huruf jar/khafadh klasik.",
            confidence = 0.96f,
            alternativeReadings = "وَحُرُوفِ الْجَرِّ",
            annotator = "Tester PDF ManuScribe",
            syncedAt = System.currentTimeMillis()
        ),
        TranscriptionEntity(
            lineId = "line_ibnsina_12r_1",
            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ ۝ قَالَ الشَّيْخُ الرَّئِيسُ أَبُو عَلِيٍّ بْنُ سِينَا رَحِمَهُ اللَّهُ",
            status = "completed",
            notes = "Invocatio (Basmalah) ditulis dengan tinta emas kaligrafi Thuluth, dilanjutkan Naskh tajam.",
            confidence = 0.98f,
            alternativeReadings = "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ",
            annotator = "Muhamad Ikbal",
            syncedAt = System.currentTimeMillis()
        ),
        TranscriptionEntity(
            lineId = "line_ibnsina_12r_2",
            text = "الْحَمْدُ لِلَّهِ حَمْدَ الشَّاكِرِينَ، وَالصَّلَاةُ عَلَى نَبِيِّهِ مُحَمَّدٍ وَآلِهِ أَجْمَعِينَ.",
            status = "completed",
            notes = "Huruf mim pada آلِهِ bersambung rapat khas ragam Mamluk.",
            confidence = 0.96f,
            alternativeReadings = "وَالصَّلَاةُ وَالسَّلَامُ عَلَى نَبِيِّهِ",
            annotator = "Muhamad Ikbal",
            syncedAt = System.currentTimeMillis()
        ),
        TranscriptionEntity(
            lineId = "line_ibnsina_12r_3",
            text = "إِنَّ الطِّبَّ عِلْمٌ يُتَعَرَّفُ مِنْهُ أَحْوَالُ بَدَنِ الْإِنْسَانِ مِنْ جِهَةِ مَا يَصِحُّ وَيَزُولُ عَنِ الصِّحَّةِ",
            status = "draft",
            notes = "Definisi sentral ilmu kedokteran klasik.",
            confidence = 0.92f,
            alternativeReadings = "عِلْمٌ يُعْرَفُ بِهِ",
            annotator = "Muhamad Ikbal",
            syncedAt = null
        )
    )
}
