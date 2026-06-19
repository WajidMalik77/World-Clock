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

# Preserve generic type signatures — required for Retrofit suspend functions.
# R8 strips Signature attributes by default, which breaks Retrofit's reflection
# on Continuation<T> parameter types at runtime (ClassCastException line 46).
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes Exceptions

# Keep all Retrofit API interfaces with their generic signatures intact
-keep,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Keep Kotlin Continuation generic type info (needed by Retrofit suspend detection)
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-keepclassmembers,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation { *; }

# Keep Retrofit core types
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.** { *; }

# --- Gson ---
# Keep the fields/constructors of any class that has @SerializedName members so
# R8 doesn't strip them, which makes Gson fail with "no-args constructor"/abstract
# class instantiation errors on JSON deserialization.
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keepclasseswithmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.google.gson.** { *; }
-keep class com.worldclock.app_themes.domain.model.** { *; }