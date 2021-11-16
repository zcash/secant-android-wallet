package co.electriccoin.zcash.configuration.model.entry

import co.electriccoin.zcash.configuration.model.map.Configuration

data class StringConfigurationEntry(
    override val key: Key,
    private val defaultValue: String
) : DefaultEntry<String> {

    override fun getValue(configuration: Configuration) = configuration.getString(key, defaultValue)
}