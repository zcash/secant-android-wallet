package co.electriccoin.zcash.configuration.model.map

import co.electriccoin.zcash.configuration.model.entry.Key
import kotlinx.datetime.Instant

/**
 * An immutable snapshot of a key-value configuration.
 */
interface Configuration {
    /**
     * @return When the configuration was updated. Null indicates the configuration either doesn't refresh or has never been refreshed.
     */
    val updatedAt: Instant?

    /**
     * @param key Key to check.
     * @return True if a mapping for `key` exists.
     */
    fun hasKey(key: Key): Boolean

    /**
     * @param key          Key to use to retrieve the value.
     * @param defaultValue Value to use if `key` doesn't exist in the
     * configuration.  Some implementations may not use strong types, and the default can also
     * be returned if type coercion fails.
     * @return boolean mapping for `key` or `defaultValue`.
     */
    fun getBoolean(key: Key, defaultValue: Boolean): Boolean

    /**
     * @param key          Key to use to retrieve the value.
     * @param defaultValue Value to use if `key` doesn't exist in the
     * configuration.  Some implementations may not use strong types, and the default can also
     * be returned if type coercion fails.
     * @return int mapping for `key` or `defaultValue`.
     */
    fun getInt(key: Key, defaultValue: Int): Int

    /**
     * @param key          Key to use to retrieve the value.
     * @param defaultValue Value to use if `key` doesn't exist in the
     * configuration.  Some implementations may not use strong types, and the default can also
     * be returned if type coercion fails.
     * @return String mapping for `key` or `defaultValue`.
     */
    fun getString(key: Key, defaultValue: String): String
}
