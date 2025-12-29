plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.rama.mako"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.rama.mako"
        minSdk = 26
        targetSdk = 36
        versionCode = 8
        versionName = "20251229_4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions += listOf("mode")

    productFlavors {
        create("dev") {
            dimension = "mode"
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        create("prod") {
            dimension = "mode"
            // No suffix for production
        }
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
        }
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // App
    implementation(libs.androidx.core.ktx)

    // Local unit tests (JVM)
    testImplementation("junit:junit:4.13.2")

    // Instrumentation tests (device / emulator)
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
