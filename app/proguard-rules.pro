# ==============================================================================
# WORKORA PRODUCTION ANTI-REVERSE-ENGINEERING & OBFUSCATION RULES (R8 / PROGUARD)
# Protects APK from JADX, APKTool, Logcat Snooping & Tampering
# ==============================================================================

# 1. Aggressive Obfuscation & Optimization Settings
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Hide original source file names (e.g. CustomerDashboardScreen.kt -> SourceFile)
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod,*Annotation*

# 2. Strip ALL Android Debug/Error Logs in Production APK (Prevents ADB Logcat Leaks)
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}

# 3. Keep Workora Data Models Safe for JSON / Firebase Serialization
-keep class com.example.model.** { *; }
-keepclassmembers class com.example.ui.screens.**$*Row { *; }
-keepclassmembers class com.example.ui.screens.**$*Item { *; }
-keepclassmembers class com.example.ui.screens.**$*Entry { *; }

# 4. Protect Android Hardware Keystore & Cryptography Classes
-keep class javax.crypto.** { *; }
-keep class java.security.** { *; }
-keep class android.security.keystore.** { *; }
-dontwarn javax.crypto.**
-dontwarn android.security.**

# 5. Jetpack Compose & Kotlin Coroutines Production Stability Rules
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# 6. Org.JSON & Networking Rules
-keep class org.json.** { *; }
-dontwarn org.json.**
