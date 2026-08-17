# TensorFlow Lite Proguard rules
-keep class org.tensorflow.lite.** { *; }
-dontwarn org.tensorflow.lite.**

# Retrofit / Gson
-keepattributes Signature
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Firebase
-keep class com.google.firebase.** { *; }
