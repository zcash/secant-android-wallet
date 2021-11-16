package co.electriccoin.zcash.configuration.test

import co.electriccoin.zcash.configuration.model.entry.Key
import co.electriccoin.zcash.configuration.model.map.Configuration
import kotlinx.datetime.Instant

/**
 * @param configurationMapping A mapping of key-value pairs to be returned
 * by [.getString].  Note: this map is not defensively copied, allowing users of this class to
 * mutate the configuration by mutating the original map. The mapping is stored in a val field
 * though, making the initial mapping thread-safe.
 */
class MockConfiguration(private val configurationMapping: Map<String, String> = emptyMap()) : Configuration {

    override val updatedAt: Instant? = null

    override fun getBoolean(
        key: Key,
        defaultValue: Boolean
    ) = configurationMapping[key.key]?.let {
        try {
            it.toBooleanStrict()
        } catch (e: IllegalArgumentException) {
            // In the future, log coercion failure as this could mean someone made an error in the remote config console
            defaultValue
        }
    } ?: defaultValue

    override fun getInt(key: Key, defaultValue: Int) = configurationMapping[key.key]?.let {
        try {
            it.toInt()
        } catch (e: NumberFormatException) {
            // In the future, log coercion failure as this could mean someone made an error in the remote config console
            defaultValue
        }
    } ?: defaultValue

    override fun getString(key: Key, defaultValue: String) = configurationMapping.getOrElse(key.key) { defaultValue }

    override fun hasKey(key: Key) = configurationMapping.containsKey(key.key)
}