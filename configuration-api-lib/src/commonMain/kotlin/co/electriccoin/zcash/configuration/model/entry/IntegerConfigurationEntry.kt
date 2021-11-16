package co.electriccoin.zcash.configuration.model.entry

import co.electriccoin.zcash.configuration.model.map.Configuration

data class IntegerConfigurationEntry(
    override val key: Key,
    private val defaultValue: Int
) : DefaultEntry<Int> {

    override fun getValue(configuration: Configuration) = configuration.getInt(key, defaultValue)
}