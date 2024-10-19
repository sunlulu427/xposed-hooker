package com.mato.http.interceptor.hooks

import com.mato.http.interceptor.beforeMethodHooker
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 * @Author: sunlulu.tomato
 * @date: 2024/10/18
 */
class ClientBuilderHook(
    lpparam: XC_LoadPackage.LoadPackageParam
) : MyXposedHook(lpparam), InvocationHandler {

    private val _builderClass by lazy {
        runCatching { classLoader.loadClass("okhttp3.OkHttpClient\$Builder") }
            .onFailure(XposedBridge::log)
            .getOrNull()
    }

    private val builderClass get() = requireNotNull(_builderClass)

    override fun onHandled() {
        XposedHelpers.findAndHookMethod(
            builderClass,
            "build",
            beforeMethodHooker(false) {
                val interceptorClazz = classLoader.loadClass("okhttp3.Interceptor")
                    ?: return@beforeMethodHooker
                val method = builderClass.getMethod("addInterceptor", interceptorClazz)
                val interceptor = Proxy.newProxyInstance(
                    classLoader,
                    arrayOf(interceptorClazz),
                    this
                )
                method.invoke(it.thisObject, interceptor)
            }
        )
    }

    override fun shouldHandle(): Boolean {
        return _builderClass != null
    }

    override fun invoke(proxy: Any?, method: Method?, args: Array<out Any>?): Any {
        return when (method?.name) {
            "intercept" -> {
                // return response
                onInterceptorIntercept(args!!.first())
            }

            else -> null
        } ?: Any()
    }

    private fun onInterceptorIntercept(chain: Any): Any? {
        val request = XposedHelpers.callMethod(chain, "request")
        val response = XposedHelpers.callMethod(chain, "proceed", request)
        val body = XposedHelpers.callMethod(response, "body")

        if (body != null && body::class.java.name.startsWith("okhttp3.")) {
            val contentType = XposedHelpers.callMethod(body, "contentType")
            if (contentType?.toString()?.contains("application/json") == true) {
                return runCatching { makeNewResponse(contentType, response, body) }
                    .onFailure(XposedBridge::log)
                    .getOrDefault(response)
            }
        }
        return response
    }

    private fun makeNewResponse(
        contentType: Any,
        response: Any,
        body: Any
    ): Any {
        val bodyString = XposedHelpers.callMethod(body, "string")
            ?: return response
        val newBody = XposedHelpers.callMethod(
            body,
            "create",
            contentType,
            bodyString
        )
        val newResponseBuilder = XposedHelpers.callMethod(
            response,
            "newBuilder"
        )
        XposedHelpers.callMethod(
            newResponseBuilder,
            "body",
            newBody
        )
        return XposedHelpers.callMethod(newResponseBuilder, "build")
    }
}