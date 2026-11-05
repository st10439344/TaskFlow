import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

// Firebase (push notifications): the plugin is only applied when google-services.json is present,
// so a fresh clone or the CI server still compiles without it. (Firebase, n.d.)
if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
}

// Release signing: the keystore details live in keystore.properties, which is NOT committed to GitHub.
val keystoreProps = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

android {
    namespace = "com.taskflow.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.taskflow.app"
        minSdk = 25 // BlueStacks Android 7.1 test copy ONLY; final project stays at API 26
        targetSdk = 36           // Google Play requires API 36 for new apps and updates from 31 Aug 2026
        versionCode = 2
        versionName = "2.0.0"


        buildConfigField("String", "API_BASE_URL", "\"https://pretty-governments-trance-dec.trycloudflare.com/\"")
        // TODO 2: the *Web application* OAuth client ID from Google Cloud (same one as GOOGLE_WEB_CLIENT_ID on Render)
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"684670544853-jvgd4pjab91b30nfe3fpgtql2ji270qr.apps.googleusercontent.com\"")
    }

    signingConfigs {
        create("release") {
            if (keystoreProps.isNotEmpty()) {
                storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            // Signed when keystore.properties exists; otherwise Gradle produces an unsigned build.
            if (keystoreProps.isNotEmpty()) signingConfig = signingConfigs.getByName("release")
        }
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    // Room (offline database)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // WorkManager (background sync + reminders)
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    // Retrofit (REST API)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Encrypted storage for the JWT
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Google Sign-In (Credential Manager)
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // Firebase Cloud Messaging (real-time push notifications). The BoM keeps the Firebase versions aligned.
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-messaging")

    testImplementation("junit:junit:4.13.2")
}
