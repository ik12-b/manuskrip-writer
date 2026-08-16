# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# --- ManuScribe: aturan defensif untuk R8 minify (build release) ---

# Room @Entity / model data classes: field-nya diakses lewat kolom SQL hasil
# codegen Room dan lewat key JSON manual (org.json) di HtrService/Repository,
# jadi nama field harus tetap utuh (jangan dihapus/di-rename R8).
-keep class com.example.data.model.** { *; }

# Hasil deserialisasi JSON manual (bukan Gson/refleksi), tapi tetap dijaga
# supaya field confidence/notes/alternativeReadings tidak dianggap "unused".
-keep class com.example.data.remote.HtrRecognitionResult { *; }

# Room sudah membawa consumer-rules sendiri untuk DAO & Database, tapi baris
# ini menjaga anotasi Room tetap ada untuk proses verifikasi skema di runtime.
-keepattributes RuntimeVisibleAnnotations, AnnotationDefault
