package cash.z.ecc.ui.preference

import android.content.Context
import cash.z.ecc.ui.util.SuspendingLazy
import co.electriccoin.zcash.preference.AndroidPreferenceProvider
import co.electriccoin.zcash.preference.api.PreferenceProvider

object StandardPreferenceSingleton {

    private const val PREF_FILENAME = "co.electriccoin.zcash"

    private val lazy = SuspendingLazy<Context, PreferenceProvider> {
        AndroidPreferenceProvider.newStandard(it, PREF_FILENAME)
    }

    suspend fun getInstance(context: Context) = lazy.getInstance(context)
}
