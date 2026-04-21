# ==================== Retrofit ====================
-keepattributes Signature
-keepattributes Exceptions
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# ==================== OkHttp ====================
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# ==================== Gson ====================
-keepattributes *Annotation*
-keepattributes RuntimeVisibleAnnotations
-keep class com.google.gson.** { *; }
-keep class com.casy.music.data.remote.dto.** { *; }

# ==================== Room ====================
# Keep abstract RoomDatabase subclass dan semua @Dao interface-nya.
# R8 kadang strip implementasi @Dao yang di-generate karena tidak ada
# referensi statis langsung ke kelas generated-nya.
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Database class * { *; }
-dontwarn androidx.room.paging.**

# ==================== Hilt ====================
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keep @androidx.hilt.work.HiltWorker class * { *; }

# Keep Hilt-generated worker factories.
# DownloadWorker menggunakan @HiltWorker + @AssistedInject — Hilt men-generate
# factory konkret saat compile time. Tanpa rule ini R8 men-strip implementasi
# konkretnya → "not a concrete class" crash saat WorkManager mencoba membuat worker.
-keep class * extends androidx.hilt.work.WorkerAssistedFactory { *; }
-keep @dagger.assisted.AssistedFactory class * { *; }
-keep class **_AssistedFactory { *; }
-keep class **_HiltModules { *; }
-keep class **_HiltModules$* { *; }

# ==================== Media3 / ExoPlayer ====================
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# ==================== WorkManager ====================
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# ==================== NewPipeExtractor ====================
-keep class org.schabi.newpipe.extractor.** { *; }
-dontwarn org.schabi.newpipe.extractor.**
-dontwarn org.mozilla.**
-dontwarn com.grack.**

# ==================== YoutubeDL-Android + FFmpeg ====================
-keep class com.yausername.youtubedl_android.** { *; }
-keep class com.yausername.ffmpeg.** { *; }
-dontwarn com.yausername.**

# ==================== Chaquopy (Python runtime) ====================
-keep class com.chaquo.python.** { *; }
-dontwarn com.chaquo.python.**

# ==================== Apache Commons Compress ====================
# FIX CRASH: YoutubeDL.initPython() menggunakan Commons Compress untuk mengekstrak
# binary Python dari ZIP. ExtraFieldUtils.<clinit> mendaftarkan semua implementasi
# ZipExtraField (termasuk AsiExtraField) via refleksi.
#
# Dari mapping.txt yang dianalisis:
#   U4.a = org.apache.commons.compress.archivers.zip.AsiExtraField  ← kelas yang crash
#   U4.D = org.apache.commons.compress.archivers.zip.ZipExtraField   ← interface-nya
#   U4.C = org.apache.commons.compress.archivers.zip.ZipEncodingHelper ← yang punya <clinit>
#
# R8 tidak bisa melihat registrasi reflektif ini → strip AsiExtraField → crash.
-keep class org.apache.commons.compress.** { *; }
-dontwarn org.apache.commons.compress.**

# ==================== Native methods ====================
-keepclasseswithmembers class * {
    native <methods>;
}

# ==================== Coroutines ====================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# ==================== General ====================
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception