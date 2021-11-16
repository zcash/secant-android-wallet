package co.electriccoin.zcash.preference.test.fixture

import co.electriccoin.zcash.preference.model.entry.PreferenceKey
import co.electriccoin.zcash.preference.model.entry.StringPreferenceDefault

object StringDefaultPreferenceFixture {
    val KEY = PreferenceKey("some_string_key") // $NON-NLS
    const val DEFAULT_VALUE = "some_default_value" // $NON-NLS
    fun new(preferenceKey: PreferenceKey = KEY, value: String = DEFAULT_VALUE) = StringPreferenceDefault(preferenceKey, value)
}
