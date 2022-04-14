package cash.z.ecc.sdk.ext.ui.model

import android.content.Context
import android.content.res.Configuration
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.SmallTest
import cash.z.ecc.android.sdk.ext.Conversions
import cash.z.ecc.sdk.model.Zatoshi
import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Test
import java.math.BigDecimal
import java.util.Locale
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ZecStringTest {

    companion object {
        private val EN_US_MONETARY_SEPARATORS = MonetarySeparators(',', '.')
        private val context = run {
            val applicationContext = ApplicationProvider.getApplicationContext<Context>()
            val enUsConfiguration = Configuration(applicationContext.resources.configuration).apply {
                setLocale(Locale.US)
            }
            applicationContext.createConfigurationContext(enUsConfiguration)
        }
    }

    @Test
    fun empty_string() {
        val actual = Zatoshi.fromZecString(context, "", EN_US_MONETARY_SEPARATORS)
        val expected = null

        assertEquals(expected, actual)
    }

    @Test
    fun decimal_monetary_separator() {
        val actual = Zatoshi.fromZecString(context, "1.13", EN_US_MONETARY_SEPARATORS)
        val expected = Zatoshi(113000000L)

        assertEquals(expected, actual)
    }

    @Test
    fun comma_grouping_separator() {
        val actual = Zatoshi.fromZecString(context, "1,130", EN_US_MONETARY_SEPARATORS)
        val expected = Zatoshi(113000000000L)

        assertEquals(expected, actual)
    }

    @Test
    fun decimal_monetary_and() {
        val actual = Zatoshi.fromZecString(context, "1,130", EN_US_MONETARY_SEPARATORS)
        val expected = Zatoshi(113000000000L)

        assertEquals(expected, actual)
    }

    @Test
    @Ignore("https://github.com/zcash/zcash-android-wallet-sdk/issues/412")
    fun toZecString() {
        val expected = "1.13000000"
        val actual = Zatoshi(113000000).toZecString()

        assertEquals(expected, actual)
    }

    @Test
    @Ignore("https://github.com/zcash/zcash-android-wallet-sdk/issues/412")
    fun round_trip() {
        val expected = Zatoshi(113000000L)
        val actual = Zatoshi.fromZecString(context, expected.toZecString(), EN_US_MONETARY_SEPARATORS)

        assertEquals(expected, actual)
    }

    @Test
    fun parse_bad_string() {
        assertNull(Zatoshi.fromZecString(context, "", EN_US_MONETARY_SEPARATORS))
        assertNull(Zatoshi.fromZecString(context, "+@#$~^&*=", EN_US_MONETARY_SEPARATORS))
        assertNull(Zatoshi.fromZecString(context, "asdf", EN_US_MONETARY_SEPARATORS))
    }

    @Test
    fun parse_invalid_numbers() {
        assertNull(Zatoshi.fromZecString(context, "", EN_US_MONETARY_SEPARATORS))
        assertNull(Zatoshi.fromZecString(context, "1,2", EN_US_MONETARY_SEPARATORS))
        assertNull(Zatoshi.fromZecString(context, "1,23,", EN_US_MONETARY_SEPARATORS))
        assertNull(Zatoshi.fromZecString(context, "1,234,", EN_US_MONETARY_SEPARATORS))
    }

    // TODO [472]: https://github.com/zcash/zcash-android-wallet-sdk/issues/472
    @Test
    @SmallTest
    fun overflow_number_test() {
        assertNotNull(Zatoshi.fromZecString(context, "1", EN_US_MONETARY_SEPARATORS))
        assertNotNull(Zatoshi.fromZecString(context, "1,000", EN_US_MONETARY_SEPARATORS))
        assertNotNull(Zatoshi.fromZecString(context, "10,000,000,000", EN_US_MONETARY_SEPARATORS))
        assertNull(Zatoshi.fromZecString(context, "100,000,000,000", EN_US_MONETARY_SEPARATORS))

        val overflowCausingNumber = 100000000000L
        assertTrue(BigDecimal(overflowCausingNumber).times(Conversions.ONE_ZEC_IN_ZATOSHI) > BigDecimal(Long.MAX_VALUE))
        assertNull(Zatoshi.fromZecString(context, overflowCausingNumber.toString(), EN_US_MONETARY_SEPARATORS))
    }
}
