plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdkVersion(34)

    defaultConfig {
        minSdkVersion(24)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    compileOnly("de.robv.android.xposed:api:82")
    compileOnly("de.robv.android.xposed:api:82:sources")
}