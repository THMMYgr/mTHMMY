# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Don't obfuscate code (due to issues in Firebase)
-dontobfuscate

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*

# Picasso
-dontwarn com.squareup.okhttp.**

# Android-Iconics (fontawesome-typeface)
-keep class .R
-keep class **.R$* {
    <fields>;
}

# android-gif-drawable
-keep public class pl.droidsonroids.gif.GifIOException{<init>(int, java.lang.String);}

# JSoup
-keep class org.jsoup.**

# Markwon
-keep class com.caverock.androidsvg.** { *; }
-dontwarn com.caverock.androidsvg.**