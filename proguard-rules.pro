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

# Keep Hilt classes
-keep class * extends dagger.hilt.internal.ComponentManager { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponentManager { *; }

# Keep Room entities
-keep class com.cryptosignal.assistant.data.local.database.entity.** { *; }
-keep class com.cryptosignal.assistant.domain.models.** { *; }

# Keep Retrofit service methods
-keep class com.cryptosignal.assistant.data.api.MEXCApiClient { *; }
-keep class com.cryptosignal.assistant.data.api.dto.** { *; }

# Keep serialization classes
-keepattributes *Annotation*
-keep class kotlin.** { *; }
-keep class kotlinx.serialization.** { *; }

# Keep TensorFlow Lite models
-keep class org.tensorflow.lite.** { *; }
-keep class * extends org.tensorflow.lite.Delegate { *; }

# Keep WorkManager workers
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }

# Keep ViewModel classes
-keep class * extends androidx.lifecycle.ViewModel { *; }

# Keep Compose UI classes
-keep class * extends androidx.compose.runtime.Composable { *; }

# Remove logging in release builds
-assumenosideeffects class timber.log.Timber {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}

# Optimize
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*