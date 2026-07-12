# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

-dontwarn com.google.errorprone.**
-keep class com.google.errorprone.** { *; }

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.oussama_chatri.productivityx.**$$serializer { *; }
-keepclassmembers class com.oussama_chatri.productivityx.** {
    *** Companion;
}
-keepclasseswithmembers class com.oussama_chatri.productivityx.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ── Gson ──────────────────────────────────────────────────────────────
-keepattributes Signature
-keep class com.google.gson.** { *; }
-keep class com.google.gson.stream.** { *; }

# Keep fields annotated with @SerializedName (CRITICAL for Retrofit/Gson)
-keepclassmembers,allowobfuscation,allowshrinking class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

-keep,allowobfuscation,allowshrinking class * extends com.google.gson.TypeAdapter
-keep,allowobfuscation,allowshrinking class * implements com.google.gson.TypeAdapterFactory
-keep,allowobfuscation,allowshrinking class * implements com.google.gson.JsonSerializer
-keep,allowobfuscation,allowshrinking class * implements com.google.gson.JsonDeserializer

# ── Retrofit ──────────────────────────────────────────────────────────
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

-keepclasseswithmembers,allowshrinking,allowobfuscation class * {
    @retrofit2.http.* <methods>;
}

# ── App DTOs (ALL features) ────────────────────────────────────────
-keep class com.oussama_chatri.productivityx.features.auth.data.remote.dto.** { *; }
-keep class com.oussama_chatri.productivityx.features.settings.data.remote.dto.** { *; }
-keep class com.oussama_chatri.productivityx.features.ai.data.remote.dto.** { *; }
-keep class com.oussama_chatri.productivityx.features.tasks.data.remote.dto.** { *; }
-keep class com.oussama_chatri.productivityx.features.notes.data.remote.dto.** { *; }
-keep class com.oussama_chatri.productivityx.features.events.data.remote.dto.** { *; }
-keep class com.oussama_chatri.productivityx.features.pomodoro.data.remote.dto.** { *; }
-keep class com.oussama_chatri.productivityx.features.search.data.remote.dto.** { *; }

# Generic API wrapper
-keep class com.oussama_chatri.productivityx.core.network.ApiResponse { *; }
-keep class com.oussama_chatri.productivityx.core.network.ApiResponse$* { *; }

# Delta sync (parsed via direct Gson, not Retrofit)
-keep class com.oussama_chatri.productivityx.core.sync.DeltaSyncEnvelope { *; }
-keep class com.oussama_chatri.productivityx.core.sync.DeltaSyncData { *; }

# Export/import payload (serialized via direct Gson)
-keep class com.oussama_chatri.productivityx.core.data.ExportPayload { *; }