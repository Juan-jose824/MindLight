plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.example.mindlight"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.mindlight"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.13" // o la que uses desde BOM
    }
}

dependencies {
    // Compose BOM
    implementation(platform(libs.compose.bom))
    implementation("com.squareup.okhttp3:okhttp:4.12.0")


    // UI base
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.compose.foundation)

    // Wear Compose
    implementation("androidx.wear.compose:compose-material:1.2.0")
    implementation(libs.wear.tooling.preview)

    // Activity y ViewModel Compose
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.viewmodel.compose)

    // Material 3
    implementation(libs.material3.android)

    // Splashscreen
    implementation(libs.core.splashscreen)

    // Google Play Services (Wear y Coroutines)
    implementation("com.google.android.gms:play-services-wearable:18.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)

    // Debug/Test
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
}
