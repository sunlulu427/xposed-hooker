package com.mato.http.interceptor

import android.app.ActivityManager
import android.content.Context
import android.os.Process

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

inline fun <T> Result<T>.onSuccessWhen(condition: Boolean, action: (value: T) -> Unit): Result<T> {
    if (condition) {
        this.onSuccess(action)
    }
    return this
}