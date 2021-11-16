package co.electriccoin.zcash.configuration.model.entry

import co.electriccoin.zcash.configuration.test.MockConfiguration
import co.electriccoin.zcash.configuration.test.fixture.BooleanDefaultEntryFixture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BooleanDefaultEntryTest {
    @Test
    fun key() {
        assertEquals(BooleanDefaultEntryFixture.KEY, BooleanDefaultEntryFixture.newTrueEntry().key)
    }

    @Test
    fun value_default_true() {
        val entry = BooleanDefaultEntryFixture.newTrueEntry()
        assertTrue(entry.getValue(MockConfiguration()))
    }

    @Test
    fun value_default_false() {
        val entry = BooleanDefaultEntryFixture.newFalseEntry()
        assertFalse(entry.getValue(MockConfiguration()))
    }

    @Test
    fun value_from_config_false() {
        val entry = BooleanDefaultEntryFixture.newTrueEntry()
        val config = MockConfiguration(mapOf(BooleanDefaultEntryFixture.KEY.key to false.toString()))
        assertFalse(entry.getValue(config))
    }

    @Test
    fun value_from_config_true() {
        val entry = BooleanDefaultEntryFixture.newTrueEntry()
        val config = MockConfiguration(mapOf(BooleanDefaultEntryFixture.KEY.key to true.toString()))
        assertTrue(entry.getValue(config))
    }
}
