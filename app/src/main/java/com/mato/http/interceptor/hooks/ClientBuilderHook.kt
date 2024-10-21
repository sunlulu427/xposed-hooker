package com.mato.http.interceptor.hooks

import android.content.Context
import com.mato.http.interceptor.DatabaseHelper
import com.mato.http.interceptor.HttpRequestEntity
import com.mato.http.interceptor.InstructionReceiver
import com.mato.http.interceptor.beforeMethodHooker
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.net.URL

/**
 * @Author: sunlulu.tomato
 * @date: 2024/10/18
 */
class ClientBuilderHook(
    context: Context,
    lpparam: XC_LoadPackage.LoadPackageParam
) : MyXposedHook(context, lpparam), InvocationHandler {

    private val _builderClass by lazy {
        runCatching { classLoader.loadClass("okhttp3.OkHttpClient\$Builder") }
            .onFailure(XposedBridge::log)
            .getOrNull()
    }
    private val builderClass get() = requireNotNull(_builderClass)
    private val databaseHelper get() = DatabaseHelper.get(context)
    private val responseBodyClass by lazy {
        XposedHelpers.findClass("okhttp3.ResponseBody", classLoader)
    }

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

    private fun isSupportedMediaType(contentType: Any?): Boolean {
        contentType ?: return false
        val str = contentType.toString()
        return str.contains("text/")
                || str.contains("/json")
                || str.contains("/xml")
    }

    private fun onInterceptorIntercept(chain: Any): Any? {
        val request = XposedHelpers.callMethod(chain, "request")
        val response = XposedHelpers.callMethod(chain, "proceed", request)
        val body = XposedHelpers.callMethod(response, "body")
            ?: return response

        val contentType = XposedHelpers.callMethod(body, "contentType")
        val isOkhttpResponseBody = responseBodyClass.isInstance(body)

        if (isOkhttpResponseBody && isSupportedMediaType(contentType)) {
            val bodyString = XposedHelpers.callMethod(body, "string")
                ?: return response
            return runCatching { makeNewResponse(contentType, response, body, bodyString) }
                .onFailure(XposedBridge::log)
                .onSuccess {
                    // record to database
                    // only hook started, continue ...
                    val entity = buildEntity(request, response, contentType, bodyString)
                    if (InstructionReceiver.hookStarted) {
                        databaseHelper.insert(entity)
                    } else {
                        databaseHelper.addToCache(entity)
                    }
                }
                .getOrDefault(response)
        }
        return response
    }

    private fun makeNewResponse(
        contentType: Any?,
        response: Any,
        body: Any,
        bodyString: Any
    ): Any {
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

    private fun buildEntity(
        request: Any,
        response: Any,
        contentType: Any?,
        bodyString: Any
    ): HttpRequestEntity {
        val httpUrl = XposedHelpers.callMethod(request, "url")
        val url = XposedHelpers.callMethod(httpUrl, "url") as URL
        val urlString = url.toString()
        val method = XposedHelpers.callMethod(request, "method") as String
        val contentTypeStr = contentType?.toString() ?: ""
        val code = XposedHelpers.callMethod(response, "code") as Int
        val ts = System.currentTimeMillis()
        return HttpRequestEntity(
            url = urlString,
            code = code,
            method = method,
            contentType = contentTypeStr,
            response = bodyString as String,
            ts = ts
        )
    }
}