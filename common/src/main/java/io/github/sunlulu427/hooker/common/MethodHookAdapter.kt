package io.github.sunlulu427.hooker.common

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
) = object : MethodLoggingHook(logBefore = log) {
    override fun beforeHookedMethod(param: MethodHookParam?) {
        super.beforeHookedMethod(param)
        param?.let(beforeMethodHooked)
    }
}

fun afterMethodHooker(
    log: Boolean = false,
    afterMethodHooked: (MethodHookParam) -> Unit
) = object : MethodLoggingHook(logAfter = log) {
    override fun beforeHookedMethod(param: MethodHookParam?) {
        super.beforeHookedMethod(param)
        param?.let(afterMethodHooked)
    }
}

object FullLoggingMethodHooker : MethodLoggingHook(logBefore = true, logAfter = true)
object BeforeLoggingMethodHooker : MethodLoggingHook(logBefore = true)
object AfterLoggingMethodHooker : MethodLoggingHook(logAfter = true)

abstract class MethodLoggingHook(
    private val logBefore: Boolean = false,
    private val logAfter: Boolean = false
) : XC_MethodHook() {
    override fun beforeHookedMethod(param: MethodHookParam?) {
        super.beforeHookedMethod(param)
        param?.takeIf { logBefore }?.onLog("before")
    }

    override fun afterHookedMethod(param: MethodHookParam?) {
        super.afterHookedMethod(param)
        param?.takeIf { logAfter }?.onLog("after")
    }

    private fun MethodHookParam.onLog(description: String) {
        val clazz = thisObject?.javaClass?.name
        val method = method?.name
        val args = args?.contentToString()
        val instance = thisObject?.hashCode()?.toString(16) ?: "null"
        XposedBridge.log("$description hooked ${clazz}@${instance} ::${method}: $args")
    }
}