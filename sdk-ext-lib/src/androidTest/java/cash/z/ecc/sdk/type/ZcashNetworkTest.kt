package cash.z.ecc.sdk.type

import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.SmallTest
import cash.z.ecc.android.sdk.model.ZcashNetwork
import org.junit.Assert.assertEquals
import org.junit.Test

class ZcashNetworkTest {
    @SmallTest
    @Test
    fun mainnet() {
        assertEquals(ZcashNetwork.Mainnet, ZcashNetwork.fromResources(ApplicationProvider.getApplicationContext()))
    }
}
