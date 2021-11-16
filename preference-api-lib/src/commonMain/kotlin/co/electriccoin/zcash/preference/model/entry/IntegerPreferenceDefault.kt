package co.electriccoin.zcash.preference.model.entry

import co.electriccoin.zcash.preference.api.PreferenceProvider

data class IntegerPreferenceDefault(
    val preferenceKey: PreferenceKey,
    private val defaultValue: Int
) : PreferenceDefault<Int> {

    override suspend fun getValue(preferenceProvider: PreferenceProvider) = preferenceProvider.getString(preferenceKey)?.let {
        try {
            it.toInt()
        } catch (e: NumberFormatException) {
            // [TODO #32]: Log coercion failure instead of just silently returning default
            defaultValue
        }
    } ?: defaultValue

    override suspend fun putValue(preferenceProvider: PreferenceProvider, newValue: Int) {
        preferenceProvider.putString(key, newValue.toString())
    }
}
