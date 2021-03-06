package co.electriccoin.zcash.ui.screen.home.model

import androidx.test.filters.SmallTest
import cash.z.ecc.android.sdk.Synchronizer
import cash.z.ecc.sdk.ext.ui.model.FiatCurrencyConversionRateState
import cash.z.ecc.sdk.ext.ui.model.toZecString
import cash.z.ecc.sdk.model.PercentDecimal
import co.electriccoin.zcash.ui.R
import co.electriccoin.zcash.ui.fixture.WalletSnapshotFixture
import co.electriccoin.zcash.ui.test.getAppContext
import co.electriccoin.zcash.ui.test.getStringResource
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertNotNull

class WalletDisplayValuesTest {

    @Test
    @SmallTest
    fun download_running_test() {
        val walletSnapshot = WalletSnapshotFixture.new(
            progress = PercentDecimal.ONE_HUNDRED_PERCENT,
            status = Synchronizer.Status.SCANNING,
            orchardBalance = WalletSnapshotFixture.ORCHARD_BALANCE,
            saplingBalance = WalletSnapshotFixture.SAPLING_BALANCE,
            transparentBalance = WalletSnapshotFixture.TRANSPARENT_BALANCE
        )
        val values = WalletDisplayValues.getNextValues(
            getAppContext(),
            walletSnapshot,
            false
        )

        assertNotNull(values)
        assertEquals(1f, values.progress.decimal)
        assertEquals(walletSnapshot.totalBalance().toZecString(), values.zecAmountText)
        assertEquals(getStringResource(R.string.home_status_syncing_catchup), values.statusText)
        // TODO [#578] https://github.com/zcash/zcash-android-wallet-sdk/issues/578
        assertEquals(FiatCurrencyConversionRateState.Unavailable, values.fiatCurrencyAmountState)
        assertEquals(getStringResource(R.string.fiat_currency_conversion_rate_unavailable), values.fiatCurrencyAmountText)
    }
}
