plugins {
    // AGP 8.9.1 is the first version that supports compileSdk 36, which Google Play now requires (targetSdk 36).
    id("com.android.application") version "8.9.1" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.28" apply false
    // Firebase Cloud Messaging configuration (applied in :app only when google-services.json exists)
    id("com.google.gms.google-services") version "4.4.2" apply false
}
