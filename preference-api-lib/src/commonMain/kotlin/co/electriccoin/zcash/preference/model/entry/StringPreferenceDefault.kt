package co.electriccoin.zcash.preference.model.entry

import co.electriccoin.zcash.preference.api.PreferenceProvider

data class StringPreferenceDefault(
    val preferenceKey: PreferenceKey,
    private val defaultValue: String
) : PreferenceDefault<String> {

    override suspend fun getValue(preferenceProvider: PreferenceProvider) = preferenceProvider.getString(preferenceKey)
        ?: defaultValue

    override suspend fun putValue(preferenceProvider: PreferenceProvider, newValue: String) {
        preferenceProvider.putString(key, newValue)
    }
}
