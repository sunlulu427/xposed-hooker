import java.time.LocalDateTime

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdkVersion(33)
    defaultConfig {
        applicationId = "com.mato.http.interceptor"
        minSdkVersion(24)
        targetSdkVersion(33)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        val date = LocalDateTime.now()
        versionName = "${date.month}/${date.dayOfMonth} ${date.hour}:${date.minute}"
        buildConfigField("String", "VERSION_NAME", "\"${versionName}\"")
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(fileTree("dir" to "libs", "include" to arrayOf("*.jar")))
    compileOnly("com.dafruits:webrtc:123.0.0")
    compileOnly("de.robv.android.xposed:api:82")
    compileOnly("de.robv.android.xposed:api:82:sources")
    compileOnly("com.squareup.okhttp3:okhttp:3.14.9")
    compileOnly("com.squareup.okhttp3:logging-interceptor:3.14.9")
}
