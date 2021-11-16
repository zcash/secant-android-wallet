package co.electriccoin.zcash.configuration.model.entry

import co.electriccoin.zcash.configuration.model.map.Configuration

data class BooleanConfigurationEntry(
    override val key: Key,
    private val defaultValue: Boolean
) : DefaultEntry<Boolean> {

    override fun getValue(configuration: Configuration) =
        configuration.getBoolean(key, defaultValue)
}
