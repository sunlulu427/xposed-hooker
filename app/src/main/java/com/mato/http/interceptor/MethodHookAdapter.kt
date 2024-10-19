package com.mato.http.interceptor

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodHook.MethodHookParam
import de.robv.android.xposed.XposedBridge

/**
 * @Author sunlulu.tomato
 * @Date 7/10/24
 */
fun beforeMethodHooker(
    log: Boolean = false,
    beforeMethodHooked: (MethodHookParam) -> Unit
) = object : MethodLoggingHook(log) {
    override fun beforeHookedMethod(param: MethodHookParam?) {
        super.beforeHookedMethod(param)
        param?.let(beforeMethodHooked)
    }
}

abstract class MethodLoggingHook(private val log: Boolean = false) : XC_MethodHook() {
    override fun beforeHookedMethod(param: MethodHookParam?) {
        super.beforeHookedMethod(param)
        if (log) {
            val clazz = param?.thisObject?.javaClass?.name
            val method = param?.method?.name
            val args = param?.args?.contentToString()
            val instance = param?.thisObject?.hashCode()?.toString(16) ?: "null"
            XposedBridge.log("hooked ${clazz}@${instance} ::${method}: $args")
        }
    }

    override fun afterHookedMethod(param: MethodHookParam?) {
        super.afterHookedMethod(param)
    }
}