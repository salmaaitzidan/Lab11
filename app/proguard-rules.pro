# Règles ProGuard pour MapTracker
# Garder les classes Google Maps
-keep class com.google.android.gms.maps.** { *; }
-keep interface com.google.android.gms.maps.** { *; }
