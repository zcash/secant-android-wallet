package co.electriccoin.zcash.preference.test.fixture

import co.electriccoin.zcash.preference.model.entry.IntegerPreferenceDefault
import co.electriccoin.zcash.preference.model.entry.PreferenceKey

object IntegerPreferenceDefaultFixture {
    val KEY = PreferenceKey("some_string_key") // $NON-NLS
    const val DEFAULT_VALUE = 123
    fun new(preferenceKey: PreferenceKey = KEY, value: Int = DEFAULT_VALUE) = IntegerPreferenceDefault(preferenceKey, value)
}
