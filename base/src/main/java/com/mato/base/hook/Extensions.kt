package com.mato.base.hook

import android.app.ActivityManager
import android.content.Context
import android.os.Process
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

/**
 * @Author: sunlulu.tomato
 * @date: 2024/10/21
 */
fun Context.getCurrentProcessName(): String? {
    val pid = Process.myPid()
    val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    for (process in activityManager.runningAppProcesses) {
        if (process.pid == pid) {
            return process.processName
        }
    }
    return null
}

fun Context.isCurrentProcessMainProcess(): Boolean {
    val processName = this.getCurrentProcessName()
    return processName != null && processName == this.packageName
}

fun LoadPackageParam.isOkhttpPresent(): Boolean {
    return kotlin.runCatching {
        XposedHelpers.findClass("okhttp3.OkHttpClient", classLoader)
    }.isSuccess
}

inline fun <T> Result<T>.onSuccessWhen(condition: Boolean, action: (value: T) -> Unit): Result<T> {
    if (condition) {
        this.onSuccess(action)
    }
    return this
}