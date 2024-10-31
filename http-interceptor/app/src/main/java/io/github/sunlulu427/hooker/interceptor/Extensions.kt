package io.github.sunlulu427.hooker.interceptor

import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

/**
 * @Author: sunlulu.tomato
 * @date: 2024/10/21
 */
fun LoadPackageParam.isOkhttpPresent(): Boolean {
    return kotlin.runCatching {
        XposedHelpers.findClass("okhttp3.OkHttpClient", classLoader)
    }.isSuccess
}
