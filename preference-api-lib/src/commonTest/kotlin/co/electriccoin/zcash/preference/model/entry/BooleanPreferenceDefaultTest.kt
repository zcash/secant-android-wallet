package co.electriccoin.zcash.preference.model.entry

import co.electriccoin.zcash.preference.test.MockPreferenceProvider
import co.electriccoin.zcash.preference.test.fixture.BooleanPreferenceDefaultFixture
import co.electriccoin.zcash.test.runBlockingTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BooleanPreferenceDefaultTest {
    @Test
    fun key() {
        assertEquals(BooleanPreferenceDefaultFixture.KEY, BooleanPreferenceDefaultFixture.newTrue().preferenceKey)
    }

    @Test
    fun value_default_true() = runBlockingTest {
        val entry = BooleanPreferenceDefaultFixture.newTrue()
        assertTrue(entry.getValue(MockPreferenceProvider()))
    }

    @Test
    fun value_default_false() = runBlockingTest {
        val entry = BooleanPreferenceDefaultFixture.newFalse()
        assertFalse(entry.getValue(MockPreferenceProvider()))
    }

    @Test
    fun value_from_config_false() = runBlockingTest {
        val entry = BooleanPreferenceDefaultFixture.newTrue()
        val mockPreferenceProvider = MockPreferenceProvider { mutableMapOf(BooleanPreferenceDefaultFixture.KEY.key to false.toString()) }
        assertFalse(entry.getValue(mockPreferenceProvider))
    }

    @Test
    fun value_from_config_true() = runBlockingTest {
        val entry = BooleanPreferenceDefaultFixture.newTrue()
        val mockPreferenceProvider = MockPreferenceProvider { mutableMapOf(BooleanPreferenceDefaultFixture.KEY.key to true.toString()) }
        assertTrue(entry.getValue(mockPreferenceProvider))
    }
}
