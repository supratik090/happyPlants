# ProGuard rules for Happy Plants

# Room Database
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep class com.hos.happy.plants.data.local.entity.** { *; }

# Data Models & JSON Deserialization (Gson)
-keepattributes Signature, *Annotation*, EnclosingMethod, InnerClasses
-keepclassmembers enum * { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.hos.happy.plants.data.model.** { *; }
-keep class com.hos.happy.plants.data.remote.model.** { *; }

# CameraX
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

