package com.mato.http.interceptor.hooks

import android.app.Application
import android.content.Context
import android.os.Build
import com.mato.http.interceptor.BuildConfig
import com.mato.http.interceptor.InstructionReceiver
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * @Author sunlulu.tomato
 * @Date 7/10/24
 */
class MainHookEntrance : IXposedHookLoadPackage {

    /**
     * This method is called when an app is loaded. It's called very early, even before
     * {@link Application#onCreate} is called.
     * Modules can set up their app-specific hooks here.
     *
     * @param lpparam Information about the app.
     * @throws Throwable Everything the callback throws is caught and logged.
     */
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        if (lpparam?.packageName != "com.xingin.xhs") {
            return
        }
        val instructionReceiver = InstructionReceiver()
        XposedHelpers.findAndHookMethod(
            Application::class.java,
            Application::onCreate.name,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam?) {
                    super.afterHookedMethod(param)
                    val context = param?.thisObject as? Context ?: return
                    XposedBridge.log("@App: $context")
                    XposedBridge.log("@Plugin version: ${BuildConfig.VERSION_NAME}")
                    XposedBridge.log("@OS version: ${Build.VERSION.SDK_INT}")

                    instructionReceiver.register(context)
                    arrayOf(
                        ClientBuilderHook(context, lpparam),
                    ).forEach {
                        it.handleLoadPackage()
                    }
                }
            }
        )

        XposedHelpers.findAndHookMethod(
            Application::class.java,
            Application::onTerminate.name,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam?) {
                    super.beforeHookedMethod(param)
                    val context = param?.thisObject as? Context ?: return
                    context.unregisterReceiver(instructionReceiver)
                }
            }
        )

        XposedHelpers.findAndHookMethod(
            Application::class.java,
            Application::onLowMemory.name,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam?) {
                    super.beforeHookedMethod(param)
                    val application = param?.thisObject as? Application ?: return
                    XposedBridge.log("$application onLowMemory")
                }
            }
        )
    }
}