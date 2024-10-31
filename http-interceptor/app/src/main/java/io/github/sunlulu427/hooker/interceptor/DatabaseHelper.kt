package io.github.sunlulu427.hooker.interceptor

import android.content.Context
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import de.robv.android.xposed.XposedBridge
import io.github.sunlulu427.hooker.common.MiscTaskScheduler
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * @Author: sunlulu.tomato
 * @date: 2024/10/20
 */
class DatabaseHelper private constructor(
    context: Context
) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    private val cache = ConcurrentLinkedQueue<HttpRequestEntity>()

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
                CREATE TABLE $TABLE_NAME (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    date TEXT,
                    ts INTEGER,
                    method TEXT,
                    content_type TEXT,
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
        db.beginTransaction()
        try {
            db.delete(TABLE_NAME, null, null)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun info(): DatabaseInfo {
        val db = this.readableDatabase
        val dbFile = File(db.path)
        val fileSize = if (dbFile.exists()) dbFile.length() else 0
        val entries = DatabaseUtils.queryNumEntries(db, TABLE_NAME)
        return DatabaseInfo(
            filePath = db.path,
            fileSize = fileSize,
            pageSize = db.pageSize,
            version = db.version,
            entries = entries,
            cache = cache.size
        )
    }

    fun addToCache(entity: HttpRequestEntity) {
        if (!cache.add(entity)) {
            XposedBridge.log("Add to cache failed: $entity")
            if (cache.size > MAX_CACHE_SIZE) {
                cache.poll()
            }
        }
    }

    fun insert(entity: HttpRequestEntity) {
        MiscTaskScheduler.handler.post(InsertEntityRunnable(entity))
    }

    fun insertAllCached() {
        val task = InsertEntityRunnable(*cache.toTypedArray(), isCached = true)
        MiscTaskScheduler.handler.post(task)
        clearCache()
    }

    fun clearCache() {
        cache.clear()
    }

    private inner class InsertEntityRunnable(
        private vararg val entities: HttpRequestEntity,
        private val isCached: Boolean = false
    ) : Runnable {
        override fun run() {
            for (entity in entities) {
                val db = this@DatabaseHelper.writableDatabase
                val contentValues = entity.toContentValues()
                val rowId = db.insert(TABLE_NAME, null, contentValues)
                if (rowId < 0) {
                    XposedBridge.log("Insert row failed: $entity")
                } else {
                    val type = entity.contentType ?: ""
                    val cachedDesc = if (isCached) "cached" else "new"
                    XposedBridge.log("Insert a $cachedDesc row ($type): ${entity.url}")
                }
            }
        }
    }

    companion object {
        private const val DB_NAME = "net_request.db"
        private const val TABLE_NAME = "requests"
        private const val DB_VERSION = 5
        private const val MAX_CACHE_SIZE = 100

        private val databaseManager = ConcurrentHashMap<String, DatabaseHelper>()

        fun get(context: Context): DatabaseHelper {
            val key = context.packageName
            val helper = databaseManager[key]
                ?: synchronized(this) {
                    val newInstance = DatabaseHelper(context)
                    databaseManager[key] = newInstance
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
        val entries: Long,
        val cache: Int
    )
}