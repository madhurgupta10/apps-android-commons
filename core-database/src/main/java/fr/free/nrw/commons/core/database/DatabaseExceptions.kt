package fr.free.nrw.commons.core.database

/**
 * Common database exceptions for better error handling.
 */

/**
 * Exception thrown when a database constraint is violated.
 */
class DatabaseConstraintException(message: String, cause: Throwable? = null) :
    Exception(message, cause)

/**
 * Exception thrown when a database entity is not found.
 */
class EntityNotFoundException(message: String) :
    Exception(message)

/**
 * Exception thrown when there's an error during database migration.
 */
class MigrationException(message: String, cause: Throwable? = null) :
    Exception("Migration failed: $message", cause)

/**
 * Exception thrown when the database is corrupted.
 */
class DatabaseCorruptedException(message: String, cause: Throwable? = null) :
    Exception("Database corrupted: $message", cause)

