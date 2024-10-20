package com.mato.http.interceptor

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.concurrent.ConcurrentHashMap

/**
 * @Author: sunlulu.tomato
 * @date: 2024/10/20
 */
class DatabaseHelper private constructor(
    context: Context
) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    companion object {
        private const val DB_NAME = "net_request.db"
        private const val TABLE_NAME = "requests"
        private const val DB_VERSION = 1

        private val databaseManager = ConcurrentHashMap<Context, DatabaseHelper>()

        fun get(context: Context): DatabaseHelper {
            val helper = databaseManager[context]
                ?: synchronized(this) {
                    val newInstance = DatabaseHelper(context)
                    databaseManager[context] = newInstance
                    return newInstance
                }
            return helper
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
                CREATE TABLE $TABLE_NAME (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    url TEXT,
                    code INTEGER,
                    method TEXT,
                    response TEXT,
                    ts INTEGER,
                    length INTEGER
                )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insert(entity: HttpRequestEntity) {
        val db = this.writableDatabase
        val contentValues = entity.toContentValues()
        db.insert(TABLE_NAME, null, contentValues)
    }
}