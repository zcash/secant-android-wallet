package co.electriccoin.zcash.configuration.model.entry

import co.electriccoin.zcash.configuration.model.map.Configuration

/**
 * An entry represents a key and a default value for the configuration.  By using an entry object,
 * multiple parts of the code can fetch the same configuration without duplication or accidental
 * variation in default value.  Clients define the key and default value together, rather than just
 * the key.
 */
/*
 * API note: the default value is not available through the public interface in order to prevent
 * clients from accidentally using the default value instead of the configuration value.
 *
 * Implementation note: although primitives would be nice, Objects don't increase memory usage much.
 * The autoboxing cache solves Booleans, and Strings are already objects, so that just leaves Integers.
 * Overall the number of Integer configuration entries is expected to be low compared to Booleans,
 * and perhaps many Integer values will also fit within the autoboxing cache.
 */
interface DefaultEntry<T> {

    val key: Key

    /**
     * @param configuration Configuration mapping to check for the key given to this entry.
     * @return The value in the configuration, or the default value if no mapping exists.
     */
    fun getValue(configuration: Configuration): T
}
