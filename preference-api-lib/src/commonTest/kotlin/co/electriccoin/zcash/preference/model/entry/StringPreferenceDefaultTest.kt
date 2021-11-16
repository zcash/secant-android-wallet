package co.electriccoin.zcash.preference.model.entry

import co.electriccoin.zcash.preference.test.MockPreferenceProvider
import co.electriccoin.zcash.preference.test.fixture.StringDefaultPreferenceFixture
import co.electriccoin.zcash.test.runBlockingTest
import kotlin.test.Test
import kotlin.test.assertEquals

class StringPreferenceDefaultTest {
    @Test
    fun key() {
        assertEquals(StringDefaultPreferenceFixture.KEY, StringDefaultPreferenceFixture.new().preferenceKey)
    }

    @Test
    fun value_default() = runBlockingTest {
        val entry = StringDefaultPreferenceFixture.new()
        assertEquals(StringDefaultPreferenceFixture.DEFAULT_VALUE, entry.getValue(MockPreferenceProvider()))
    }

    @Test
    fun value_override() = runBlockingTest {
        val entry = StringDefaultPreferenceFixture.new()

        val mockPreferenceProvider = MockPreferenceProvider { mutableMapOf(StringDefaultPreferenceFixture.KEY.key to "override") }

        assertEquals("override", entry.getValue(mockPreferenceProvider))
    }
}
