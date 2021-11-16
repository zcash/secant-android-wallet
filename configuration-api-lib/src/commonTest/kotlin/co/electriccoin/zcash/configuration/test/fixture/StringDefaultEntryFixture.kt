package co.electriccoin.zcash.configuration.test.fixture

import co.electriccoin.zcash.configuration.model.entry.Key
import co.electriccoin.zcash.configuration.model.entry.StringDefaultEntry

object StringDefaultEntryFixture {
    val KEY = Key("some_string_key") //$NON-NLS
    const val DEFAULT_VALUE = "some_default_value" //$NON-NLS
    fun newEntryEntry(key: Key = KEY, value: String = DEFAULT_VALUE) = StringDefaultEntry(key, value)
}