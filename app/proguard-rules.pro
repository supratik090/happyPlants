# ProGuard rules for Happy Plants

# Room Database
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Gson Models
-keepclassmembers class com.hos.happy.plants.data.remote.model.** { <fields>; }
-keep class com.hos.happy.plants.data.remote.model.** { *; }

# CameraX
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**
