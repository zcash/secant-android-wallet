package co.electriccoin.zcash.configuration.model.map

import co.electriccoin.zcash.configuration.model.entry.Key
import kotlinx.datetime.Instant

class StringConfiguration(configurationMapping: Map<String, String>, override val updatedAt: Instant?) : Configuration {

    private val configurationMapping: Map<String, String> = configurationMapping.toMap(mutableMapOf())

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
