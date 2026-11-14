# Keep kotlinx.serialization generated serializers
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.vanguard.grc.** {
    *** Companion;
}
-keepclasseswithmembers class com.vanguard.grc.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.vanguard.grc.**$$serializer { *; }
-keepclassmembers class com.vanguard.grc.** {
    *** Companion;
}
-keepclasseswithmembers class com.vanguard.grc.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Room
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-dontwarn androidx.room.paging.**
