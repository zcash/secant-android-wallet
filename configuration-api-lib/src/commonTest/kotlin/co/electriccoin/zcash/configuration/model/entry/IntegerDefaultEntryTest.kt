package co.electriccoin.zcash.configuration.model.entry

import co.electriccoin.zcash.configuration.test.MockConfiguration
import co.electriccoin.zcash.configuration.test.fixture.IntegerDefaultEntryFixture
import kotlin.test.Test
import kotlin.test.assertEquals

class IntegerDefaultEntryTest {
    @Test
    fun key() {
        assertEquals(IntegerDefaultEntryFixture.KEY, IntegerDefaultEntryFixture.newEntry().key)
    }

    @Test
    fun value_default() {
        val entry = IntegerDefaultEntryFixture.newEntry()
        assertEquals(IntegerDefaultEntryFixture.DEFAULT_VALUE, entry.getValue(MockConfiguration()))
    }

    @Test
    fun value_override() {
        val expected = IntegerDefaultEntryFixture.DEFAULT_VALUE + 5

        val entry = IntegerDefaultEntryFixture.newEntry()
        assertEquals(expected, entry.getValue(MockConfiguration(mapOf(IntegerDefaultEntryFixture.KEY.key to expected.toString()))))
    }
}
