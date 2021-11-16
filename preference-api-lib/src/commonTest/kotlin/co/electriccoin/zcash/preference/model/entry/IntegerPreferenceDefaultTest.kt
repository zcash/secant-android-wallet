package co.electriccoin.zcash.preference.model.entry

import co.electriccoin.zcash.preference.test.MockPreferenceProvider
import co.electriccoin.zcash.preference.test.fixture.IntegerPreferenceDefaultFixture
import co.electriccoin.zcash.preference.test.fixture.StringDefaultPreferenceFixture
import co.electriccoin.zcash.test.runBlockingTest
import kotlin.test.Test
import kotlin.test.assertEquals

class IntegerPreferenceDefaultTest {
    @Test
    fun key() {
        assertEquals(IntegerPreferenceDefaultFixture.KEY, IntegerPreferenceDefaultFixture.new().preferenceKey)
    }

    @Test
    fun value_default() = runBlockingTest {
        val entry = IntegerPreferenceDefaultFixture.new()
        assertEquals(IntegerPreferenceDefaultFixture.DEFAULT_VALUE, entry.getValue(MockPreferenceProvider()))
    }

    @Test
    fun value_override() = runBlockingTest {
        val expected = IntegerPreferenceDefaultFixture.DEFAULT_VALUE + 5

        val entry = IntegerPreferenceDefaultFixture.new()
        val mockPreferenceProvider = MockPreferenceProvider { mutableMapOf(StringDefaultPreferenceFixture.KEY.key to expected.toString()) }

        assertEquals(expected, entry.getValue(mockPreferenceProvider))
    }
}
