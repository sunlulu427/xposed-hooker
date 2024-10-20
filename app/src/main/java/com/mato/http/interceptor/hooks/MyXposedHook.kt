package com.mato.http.interceptor.hooks

import android.content.Context
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * @Author: sunlulu.tomato
 * @date: 2024/10/19
 */
sealed class MyXposedHook(
    protected val context: Context,
    protected val lpparam: XC_LoadPackage.LoadPackageParam
) {
    protected val classLoader: ClassLoader
        get() = lpparam.classLoader

    fun handleLoadPackage() {
        if (shouldHandle()) {
            XposedBridge.log("$this handle load package")
            onHandled()
        } else {
            XposedBridge.log("$this skipped.")
        }
    }

    protected abstract fun onHandled()

    protected open fun shouldHandle(): Boolean = true
}