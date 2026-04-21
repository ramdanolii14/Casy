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
-keep class com.google.gson.** { *; }
-keep class com.casy.music.data.remote.dto.** { *; }

# ==================== Room ====================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ==================== Hilt ====================
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keep @androidx.hilt.work.HiltWorker class * { *; }

# FIX: Keep Hilt-generated worker factories.
# R8 sebelumnya men-strip kelas konkret factory yang di-generate Hilt
# sehingga hanya interface-nya yang tersisa → "not a concrete class" crash.
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

# ==================== YoutubeDL-Android ====================
-keep class com.yausername.youtubedl_android.** { *; }
-dontwarn com.yausername.youtubedl_android.**

# ==================== Coroutines ====================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# ==================== General ====================
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception