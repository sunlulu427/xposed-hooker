package com.mato.http.interceptor

import android.content.ContentValues
import java.text.SimpleDateFormat
import java.util.Locale

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
    companion object {
        private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
    }

    private val date: String get() = formatter.format(ts)

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