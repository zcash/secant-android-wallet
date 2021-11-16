package co.electriccoin.zcash.preference.test

import co.electriccoin.zcash.preference.api.PreferenceProvider
import co.electriccoin.zcash.preference.model.entry.PreferenceKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * @param mutableMapFactory Emits a new mutable map.  Thread safety depends on the factory implementation.
 */
class MockPreferenceProvider(mutableMapFactory: () -> MutableMap<String, String> = { mutableMapOf() }) : PreferenceProvider {

    private val map = mutableMapFactory()

    override suspend fun getString(preferenceKey: PreferenceKey) = map[preferenceKey.key]

    // For the mock implementation, does not support observability of changes
    override suspend fun observe(preferenceKey: PreferenceKey): Flow<String?> = flowOf(getString(preferenceKey))

    override suspend fun hasKey(preferenceKey: PreferenceKey) = map.containsKey(preferenceKey.key)

    override suspend fun putString(preferenceKey: PreferenceKey, value: String) {
        map[preferenceKey.key] = value
    }
}
