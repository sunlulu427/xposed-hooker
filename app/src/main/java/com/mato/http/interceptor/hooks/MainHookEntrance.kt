package com.mato.http.interceptor.hooks

import android.app.Application
import android.os.Build
import com.mato.http.interceptor.BuildConfig
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
        XposedHelpers.findAndHookConstructor(
            Application::class.java,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam?) {
                    super.afterHookedMethod(param)
                    val application = param?.thisObject as Application
                    XposedBridge.log("@App: $application")
                    XposedBridge.log("@Plugin version: ${BuildConfig.VERSION_NAME}")
                    XposedBridge.log("@OS version: ${Build.VERSION.SDK_INT}")

                    arrayOf(
                        ClientBuilderHook(application, lpparam),
                    ).forEach {
                        it.handleLoadPackage()
                    }
                }
            }
        )
    }
}