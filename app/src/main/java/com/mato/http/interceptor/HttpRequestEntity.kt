package com.mato.http.interceptor

import android.content.ContentValues
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * @Author: sunlulu.tomato
 * @date: 2024/10/20
 */
class HttpRequestEntity(
    private val url: String,
    private val code: Int,
    private val method: String,
    private val response: String,
    private val ts: Long
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
        cv.put("response", response)
        cv.put("ts", ts)
        cv.put("length", response.length)
        return cv
    }
}