package io.github.sunlulu427.hooker.common

import android.app.ActivityManager
import android.content.Context
import android.os.Process

/**
 * @Author: sunlulu.tomato
 * @date: 2024/10/31
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
