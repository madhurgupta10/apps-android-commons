package fr.free.nrw.commons.core.database

/**
 * Base interface for Database Access Objects (DAOs).
 * Provides common CRUD operations that can be implemented by specific DAOs.
 *
 * This is designed to work with Room or any other database implementation.
 *
 * Note: Uses @JvmSuppressWildcards to ensure Room-generated implementations
 * properly override these methods without type variance issues.
 */
interface BaseDao<T> {

    /**
     * Insert a single entity into the database.
     * @return The row ID of the inserted entity
     */
    suspend fun insert(entity: T): Long

    /**
     * Insert multiple entities into the database.
     * Note: Room may not return row IDs for batch inserts
     */
    suspend fun insertAll(entities: @JvmSuppressWildcards List<T>)

    /**
     * Update an existing entity in the database.
     * @return Number of rows updated
     */
    suspend fun update(entity: T): Int

    /**
     * Delete an entity from the database.
     * @return Number of rows deleted
     */
    suspend fun delete(entity: T): Int

    /**
     * Delete all entities from the table.
     * @return Number of rows deleted
     */
    suspend fun deleteAll(): Int
}

