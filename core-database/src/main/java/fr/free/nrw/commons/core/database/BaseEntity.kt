package fr.free.nrw.commons.core.database

/**
 * Base entity interface for database entities.
 * Provides a common structure for entities with an ID.
 */
interface BaseEntity {
    /**
     * Unique identifier for the entity.
     * This can be implemented by concrete entities.
     */
    val id: Long
}

