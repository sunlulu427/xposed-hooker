package com.mato.http.interceptor

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import de.robv.android.xposed.XposedBridge
import java.io.File
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
        private const val DB_VERSION = 2

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

    data class DatabaseInfo(
        val filePath: String,
        val fileSize: Long,
        val pageSize: Long,
        val version: Int,
    )

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
                CREATE TABLE $TABLE_NAME (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    date TEXT,
                    ts INTEGER,
                    method TEXT,
                    code INTEGER,
                    length INTEGER,
                    url TEXT,
                    response TEXT
                )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun deleteAll() {
        val db = this.writableDatabase
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
    }

    fun info(): DatabaseInfo {
        val db = this.readableDatabase
        val dbFile = File(db.path)
        val fileSize = if (dbFile.exists()) dbFile.length() else 0
        return DatabaseInfo(
            filePath = db.path,
            fileSize = fileSize,
            pageSize = db.pageSize,
            version = db.version,
        )
    }

    fun insert(entity: HttpRequestEntity) {
        val db = this.writableDatabase
        val contentValues = entity.toContentValues()
        val rowId = db.insert(TABLE_NAME, null, contentValues)
        if (rowId < 0) {
            XposedBridge.log("Insert row failed: $entity")
        }
    }
}