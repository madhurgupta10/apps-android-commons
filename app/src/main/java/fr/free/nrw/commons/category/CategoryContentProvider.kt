package fr.free.nrw.commons.category

import android.content.ContentValues
import android.content.UriMatcher
import android.content.UriMatcher.NO_MATCH
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.text.TextUtils
import androidx.core.net.toUri
import androidx.sqlite.db.SupportSQLiteQueryBuilder
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import fr.free.nrw.commons.BuildConfig
import fr.free.nrw.commons.category.CategoryTable.ALL_FIELDS
import fr.free.nrw.commons.category.CategoryTable.COLUMN_ID
import fr.free.nrw.commons.category.CategoryTable.TABLE_NAME
import fr.free.nrw.commons.data.DBOpenHelper
import fr.free.nrw.commons.di.CommonsDaggerContentProvider

/**
 * Entry point for injecting dependencies into CategoryContentProvider
 * ContentProviders cannot use @AndroidEntryPoint, so we use @EntryPoint instead
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface CategoryContentProviderEntryPoint {
    fun dbOpenHelper(): DBOpenHelper
}

class CategoryContentProvider : CommonsDaggerContentProvider() {
    override fun onCreate(): Boolean {
        val entryPoint = EntryPointAccessors.fromApplication(
            context!!.applicationContext,
            CategoryContentProviderEntryPoint::class.java
        )
        dbOpenHelper = entryPoint.dbOpenHelper()
        return true
    }

    private val uriMatcher = UriMatcher(NO_MATCH).apply {
        addURI(BuildConfig.CATEGORY_AUTHORITY, BASE_PATH, CATEGORIES)
        addURI(BuildConfig.CATEGORY_AUTHORITY, "${BASE_PATH}/#", CATEGORIES_ID)
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?,
                       selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        val uriType = uriMatcher.match(uri)
        val db = requireDb()
        val query = when (uriType) {
            CATEGORIES -> SupportSQLiteQueryBuilder.builder(TABLE_NAME)
                .selection(selection, selectionArgs)
                .columns(projection)
                .orderBy(sortOrder)
                .create()
            CATEGORIES_ID -> SupportSQLiteQueryBuilder.builder(TABLE_NAME)
                .selection("_id = ?", arrayOf(uri.lastPathSegment))
                .columns(ALL_FIELDS)
                .orderBy(sortOrder)
                .create()
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
        val cursor = db.query(query)
        cursor?.setNotificationUri(context!!.contentResolver, uri)
        return cursor
    }

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, contentValues: ContentValues?): Uri {
        val uriType = uriMatcher.match(uri)
        var id: Long = 0L
        val db = requireDb()
        when (uriType) {
            CATEGORIES -> {
                contentValues?.let {
                    id = db.insert(TABLE_NAME, 0, it)
                }
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
        context!!.contentResolver?.notifyChange(uri, null)
        return "${BASE_URI}/$id".toUri()
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0

    override fun bulkInsert(uri: Uri, values: Array<ContentValues>): Int {
        val uriType = uriMatcher.match(uri)
        val db = requireDb()
        db.beginTransaction()
        try {
            when (uriType) {
                CATEGORIES -> {
                    for (value in values) {
                        db.insert(TABLE_NAME, 0, value)
                    }
                    db.setTransactionSuccessful()
                }
                else -> throw IllegalArgumentException("Unknown URI: $uri")
            }
        } finally {
            db.endTransaction()
        }
        context!!.contentResolver?.notifyChange(uri, null)
        return values.size
    }

    override fun update(uri: Uri, contentValues: ContentValues?, selection: String?,
                        selectionArgs: Array<String>?): Int {
        val uriType = uriMatcher.match(uri)
        var rowsUpdated = 0
        val db = requireDb()
        when (uriType) {
            CATEGORIES_ID -> {
                if (TextUtils.isEmpty(selection)) {
                    val id = uri.lastPathSegment?.toInt()
                        ?: throw IllegalArgumentException("Invalid ID")
                    contentValues?.let {
                        rowsUpdated = db.update(
                            TABLE_NAME,
                            0,
                            it,
                            "$COLUMN_ID = ?",
                            arrayOf(id.toString())
                        )
                    }
                } else {
                    throw IllegalArgumentException(
                        "Parameter `selection` should be empty when updating an ID")
                }
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri with type $uriType")
        }
        context!!.contentResolver?.notifyChange(uri, null)
        return rowsUpdated
    }

    companion object {
        fun uriForId(id: Int): Uri = Uri.parse("${BASE_URI}/$id")
        private const val CATEGORIES = 1
        private const val CATEGORIES_ID = 2
        private const val BASE_PATH = "categories"
        val BASE_URI: Uri = "content://${BuildConfig.CATEGORY_AUTHORITY}/${BASE_PATH}".toUri()
    }
}
