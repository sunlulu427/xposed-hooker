package io.github.sunlulu427.hooker.interceptor

import android.content.ContentValues
import io.github.sunlulu427.hooker.common.Formatter

/**
 * @Author: sunlulu.tomato
 * @date: 2024/10/20
 */
class HttpRequestEntity(
    val url: String,
    val code: Int,
    val method: String,
    val contentType: String?,
    val response: String,
    val ts: Long
) {
    private val date: String get() = Formatter.simpleFormatter1.format(ts)

    fun toContentValues(): ContentValues {
        val cv = ContentValues()
        cv.put("date", date)
        cv.put("url", url)
        cv.put("code", code)
        cv.put("method", method)
        cv.put("content_type", contentType ?: "")
        cv.put("response", response)
        cv.put("ts", ts)
        cv.put("length", response.length)
        return cv
    }
}