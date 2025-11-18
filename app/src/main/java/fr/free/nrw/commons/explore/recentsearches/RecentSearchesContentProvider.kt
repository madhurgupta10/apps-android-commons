package fr.free.nrw.commons.explore.recentsearches

import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import androidx.core.net.toUri
import androidx.sqlite.db.SupportSQLiteQueryBuilder
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import fr.free.nrw.commons.BuildConfig
import fr.free.nrw.commons.data.DBOpenHelper
import fr.free.nrw.commons.di.CommonsDaggerContentProvider
import fr.free.nrw.commons.explore.recentsearches.RecentSearchesTable.ALL_FIELDS
import fr.free.nrw.commons.explore.recentsearches.RecentSearchesTable.COLUMN_ID
import fr.free.nrw.commons.explore.recentsearches.RecentSearchesTable.TABLE_NAME

/**
 * Entry point for injecting dependencies into RecentSearchesContentProvider
 * ContentProviders cannot use @AndroidEntryPoint, so we use @EntryPoint instead
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface RecentSearchesContentProviderEntryPoint {
    fun dbOpenHelper(): DBOpenHelper
}

/**
 * This class contains functions for executing queries for
 * inserting, searching, deleting, editing recent searches in SqLite DB
 */
class RecentSearchesContentProvider : CommonsDaggerContentProvider() {

    override fun onCreate(): Boolean {
        // Initialize dbOpenHelper using EntryPoint since ContentProviders don't support @AndroidEntryPoint
        val entryPoint = EntryPointAccessors.fromApplication(
            context!!.applicationContext,
            RecentSearchesContentProviderEntryPoint::class.java
        )
        dbOpenHelper = entryPoint.dbOpenHelper()
        return true
    }

    /**
     * This functions executes query for searching recent searches in SqLite DB
     */
    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor {
        val queryBuilder = SupportSQLiteQueryBuilder.builder(TABLE_NAME)

        val uriType = uriMatcher.match(uri)

        val db = requireDb()
        val cursor = when (uriType) {
            RECENT_SEARCHES -> db.query(
                queryBuilder.columns(projection)
                    .selection(selection, selectionArgs)
                    .orderBy(sortOrder)
                    .create()
            )

            RECENT_SEARCHES_ID -> db.query(
                queryBuilder.columns(ALL_FIELDS)
                    .selection(
                        "$COLUMN_ID = ?", arrayOf(uri.lastPathSegment)
                    )
                    .orderBy(sortOrder)
                    .create()
            )

            else -> throw IllegalArgumentException("Unknown URI$uri")
        }

        cursor.setNotificationUri(context?.contentResolver, uri)

        return cursor
    }

    override fun getType(uri: Uri): String? = null

    /**
     * This functions executes query for inserting a recentSearch object in SqLite DB
     */
    override fun insert(uri: Uri, contentValues: ContentValues?): Uri? {
        val uriType = uriMatcher.match(uri)
        val db = requireDb()
        val id: Long? = when (uriType) {
            RECENT_SEARCHES -> contentValues?.let {
                db.insert(TABLE_NAME, 0, it)
            }

            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
        context?.contentResolver?.notifyChange(uri, null)
        return "$BASE_URI/$id".toUri()
    }

    /**
     * This functions executes query for deleting a recentSearch object in SqLite DB
     */
    override fun delete(uri: Uri, s: String?, strings: Array<String>?): Int {
        val db = requireDb()
        val rows: Int
        val uriType = uriMatcher.match(uri)
        when (uriType) {
            RECENT_SEARCHES_ID -> {
                rows = db.delete(
                    TABLE_NAME,
                    "_id = ?",
                    arrayOf(uri.lastPathSegment)
                )
            }

            else -> throw IllegalArgumentException("Unknown URI - $uri")
        }
        context?.contentResolver?.notifyChange(uri, null)
        return rows
    }

    /**
     * This functions executes query for inserting multiple recentSearch objects in SqLite DB
     */
    override fun bulkInsert(uri: Uri, values: Array<ContentValues>): Int {
        val uriType = uriMatcher.match(uri)
        val db = requireDb()
        db.beginTransaction()
        when (uriType) {
            RECENT_SEARCHES -> for (value in values) {
                db.insert(TABLE_NAME, 0, value)
            }

            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
        db.setTransactionSuccessful()
        db.endTransaction()
        context?.contentResolver?.notifyChange(uri, null)
        return values.size
    }

    /**
     * This functions executes query for updating a particular recentSearch object in SqLite DB
     */
    override fun update(
        uri: Uri, contentValues: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        val db = requireDb()
        val uriType = uriMatcher.match(uri)
        var rowsUpdated: Int = 0
        when (uriType) {
            RECENT_SEARCHES_ID -> if (selection.isNullOrEmpty()) {
                val id = uri.lastPathSegment!!.toInt()
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
                    "Parameter `selection` should be empty when updating an ID"
                )
            }

            else -> throw IllegalArgumentException("Unknown URI: $uri with type $uriType")
        }
        context?.contentResolver?.notifyChange(uri, null)
        return rowsUpdated
    }

    companion object {
        // For URI matcher
        private const val RECENT_SEARCHES = 1
        private const val RECENT_SEARCHES_ID = 2
        private const val BASE_PATH = "recent_searches"

        @JvmField
        val BASE_URI: Uri = "content://${BuildConfig.RECENT_SEARCH_AUTHORITY}/$BASE_PATH".toUri()

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)

        init {
            uriMatcher.addURI(BuildConfig.RECENT_SEARCH_AUTHORITY, BASE_PATH, RECENT_SEARCHES)
            uriMatcher.addURI(BuildConfig.RECENT_SEARCH_AUTHORITY, "$BASE_PATH/#", RECENT_SEARCHES_ID)
        }

        @JvmStatic
        fun uriForId(id: Int): Uri = "$BASE_URI/$id".toUri()
    }
}

