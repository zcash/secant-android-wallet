package co.electriccoin.zcash.configuration.test.fixture

import co.electriccoin.zcash.configuration.model.entry.IntegerDefaultEntry
import co.electriccoin.zcash.configuration.model.entry.Key
import co.electriccoin.zcash.configuration.model.entry.StringDefaultEntry

object IntegerDefaultEntryFixture {
    val KEY = Key("some_string_key") //$NON-NLS
    const val DEFAULT_VALUE = 123
    fun newEntry(key: Key = KEY, value: Int = DEFAULT_VALUE) = IntegerDefaultEntry(key, value)
}