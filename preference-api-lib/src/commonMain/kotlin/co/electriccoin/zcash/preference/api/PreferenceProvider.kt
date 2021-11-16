package co.electriccoin.zcash.preference.api

import co.electriccoin.zcash.preference.model.entry.PreferenceKey
import kotlinx.coroutines.flow.Flow

interface PreferenceProvider {

    suspend fun hasKey(preferenceKey: PreferenceKey): Boolean

    suspend fun putString(preferenceKey: PreferenceKey, value: String)

    suspend fun getString(preferenceKey: PreferenceKey): String?

    suspend fun observe(preferenceKey: PreferenceKey): Flow<String?>
}
