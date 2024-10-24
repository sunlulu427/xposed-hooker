package com.mato.base.hook

import android.os.Handler
import android.os.HandlerThread
import de.robv.android.xposed.XposedBridge

/**
 * @Author: sunlulu.tomato
 * @date: 2024/10/21
 */
object MiscTaskScheduler {
    private val thread: HandlerThread by lazy {
        HandlerThread("MiscTaskScheduler").also {
            it.start()
            XposedBridge.log("$it started")
        }
    }

    val handler by lazy { Handler(thread.looper) }

    fun isCurrentThread(): Boolean {
        return Thread.currentThread() == thread
    }
}