package io.github.sunlulu427.hooker.interceptor

import android.app.Application
import android.content.Context
import android.os.Build
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.sunlulu427.hooker.common.MyXposedHook
import io.github.sunlulu427.hooker.common.afterMethodHooker
import io.github.sunlulu427.hooker.common.beforeMethodHooker
import io.github.sunlulu427.hooker.common.isCurrentProcessMainProcess

/**
 * @Author sunlulu.tomato
 * @Date 7/10/24
 */
class MainHookEntrance : IXposedHookLoadPackage {

    /**
     * This method is called when an app is loaded. It's called very early, even before
     * {@link Application#onCreate} is called.
     * Modules can set up their app-specific hooks here.
     * Only main process of an app will be hooked. [Context.isCurrentProcessMainProcess]
     *
     * @param lpparam Information about the app.
     * @throws Throwable Everything the callback throws is caught and logged.
     */
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        if (lpparam?.isOkhttpPresent() != true) {
            return
        }
        val instructionReceiver = InstructionReceiver()
        XposedHelpers.findAndHookMethod(
            Application::class.java,
            Application::onCreate.name,
            afterMethodHooker {
                val context = it.thisObject as Context
                if (!context.isCurrentProcessMainProcess()) {
                    XposedBridge.log("$context is not main process, skipped")
                    return@afterMethodHooker
                }
                XposedBridge.log("@App: $context")
                XposedBridge.log("@Plugin version: ${BuildConfig.VERSION_NAME}")
                XposedBridge.log("@OS version: ${Build.VERSION.SDK_INT}")

                instructionReceiver.register(context)
                val hooks = mutableListOf<MyXposedHook>()
                hooks.add(ClientBuilderHook(context, lpparam))
                hooks.forEach(MyXposedHook::handleLoadPackage)
            }
        )

        XposedHelpers.findAndHookMethod(
            Application::class.java,
            Application::onTerminate.name,
            beforeMethodHooker {
                val context = it.thisObject as Context
                if (!context.isCurrentProcessMainProcess()) {
                    return@beforeMethodHooker
                }
                context.unregisterReceiver(instructionReceiver)
                val helper = DatabaseHelper.get(context)
                helper.clearCache()
            }
        )

        XposedHelpers.findAndHookMethod(
            Application::class.java,
            Application::onLowMemory.name,
            beforeMethodHooker {
                val application = it.thisObject as Application
                if (!application.isCurrentProcessMainProcess()) {
                    return@beforeMethodHooker
                }
                XposedBridge.log("$application onLowMemory")
            }
        )
    }
}