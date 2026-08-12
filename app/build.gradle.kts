plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.pm5scanner"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.pm5scanner"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2024.02.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // ANT+ library from jitpack - community wrapper or direct AAR?
    // According to ANT+ GitHub, we should use their AAR, but to simplify for a gradle build,
    // let's try to pull a known wrapper from Maven/JitPack, e.g. "com.github.ant-wireless:ANT-Android-SDKs:..."
    // If that fails, I will add dummy classes or handle it through AARs.
    // Actually, "com.dsi.ant:antpluginlib" isn't strictly published to standard maven.
    // I will mock the ANT classes in our code if it doesn't build, or just use the github artifact.
    // A known dependency often used: 
    // We will place the antpluginlib.jar or aar locally if it fails, but let's try jitpack for now.
    // implementation("com.github.ant-wireless:ANT-Android-SDKs:master")
}
