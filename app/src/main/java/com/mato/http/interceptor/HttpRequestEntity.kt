package com.mato.http.interceptor

import android.content.ContentValues

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

    fun toContentValues(): ContentValues {
        val cv = ContentValues()
        cv.put("url", url)
        cv.put("code", code)
        cv.put("method", method)
        cv.put("response", response)
        cv.put("ts", ts)
        cv.put("length", response.length)
        return cv
    }
}