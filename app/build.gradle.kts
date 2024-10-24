import com.android.build.gradle.internal.api.ApkVariantOutputImpl
import java.text.SimpleDateFormat
import java.util.Locale

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdkVersion(34)
    defaultConfig {
        applicationId = "com.mato.http.interceptor"
        minSdkVersion(24)
        targetSdkVersion(34)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.ENGLISH)
        versionName = formatter.format(System.currentTimeMillis())
        buildConfigField("String", "VERSION_NAME", "\"${versionName}\"")
    }
    applicationVariants.all {
        outputs.filterIsInstance<ApkVariantOutputImpl>()
            .forEach {
                val variant = buildType.name
                val apkName = "http_interceptor_${variant}_${versionName}.apk"
                it.outputFileName = apkName
            }
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(fileTree("dir" to "libs", "include" to arrayOf("*.jar")))
    api(project(":okhttp3"))
    compileOnly("com.dafruits:webrtc:123.0.0")
    compileOnly("de.robv.android.xposed:api:82")
    compileOnly("de.robv.android.xposed:api:82:sources")
    compileOnly("com.squareup.okhttp3:okhttp:3.14.9")
    compileOnly("com.squareup.okhttp3:logging-interceptor:3.14.9")
}
