package com.mato.http.interceptor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import de.robv.android.xposed.XposedBridge

/**
 * @Author: sunlulu.tomato
 * @date: 2024/10/20
 */
class InstructionReceiver : BroadcastReceiver() {

    companion object {
        @Volatile
        var hookStarted: Boolean = false
            private set
    }

    fun register(context: Context) {
        val filter = IntentFilter()
        for (instruction in Instruction.values()) {
            filter.addAction(instruction.action)
        }
        context.registerReceiver(this, filter)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) {
            return
        }
        // default thread is main thread
        val action = intent.action ?: return
        val instruction = Instruction.of(action) ?: return
        when (instruction) {
            Instruction.START -> {
                hookStarted = true
                XposedBridge.log("hook started")
            }

            Instruction.STOP -> {
                hookStarted = false
                XposedBridge.log("hook stopped")
            }

            Instruction.INFO -> {
                val info = DatabaseHelper.get(context).info()
                XposedBridge.log(info.toString())
            }

            Instruction.DELETE -> {
                hookStarted = false
                val helper = DatabaseHelper.get(context)
                runCatching {
                    helper.deleteAll()
                }.onSuccess {
                    XposedBridge.log("$helper deleted all")
                }.onFailure {
                    XposedBridge.log(it)
                }
            }
        }
    }

    enum class Instruction(val action: String) {
        DELETE("com.mato.http.interceptor.delete"),
        START("com.mato.http.interceptor.start"),
        STOP("com.mato.http.interceptor.stop"),
        INFO("com.mato.http.interceptor.info");

        companion object {
            fun of(action: String): Instruction? = values().firstOrNull { it.action == action }
        }
    }
}