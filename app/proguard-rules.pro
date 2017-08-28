# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\Ragnar\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
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
-dontwarn okio.**
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.ParametersAreNonnullByDefault

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