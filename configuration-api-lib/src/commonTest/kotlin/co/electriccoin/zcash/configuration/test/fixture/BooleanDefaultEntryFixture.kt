package co.electriccoin.zcash.configuration.test.fixture

import co.electriccoin.zcash.configuration.model.entry.BooleanDefaultEntry
import co.electriccoin.zcash.configuration.model.entry.Key

object BooleanDefaultEntryFixture {

    val KEY = Key("some_boolean_key") //$NON-NLS

    fun newTrueEntry() = BooleanDefaultEntry(KEY, true)

    fun newFalseEntry() = BooleanDefaultEntry(KEY, false)
}