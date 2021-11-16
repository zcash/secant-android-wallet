package co.electriccoin.zcash.preference

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import co.electriccoin.zcash.preference.api.PreferenceProvider
import co.electriccoin.zcash.preference.model.entry.PreferenceKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

/**
 * Provides encrypted shared preferences.
 *
 * This class is thread-safe.
 *
 * For a given preference file, it is expected that only a single instance is constructed and that
 * this instance lives for the lifetime of the application. Constructing multiple instances will
 * potentially corrupt preference data and will leak resources.
 */
/*
 * Implementation note: EncryptedSharedPreferences are not thread-safe, so this implementation
 * confines them to a single background thread.
 */
class EncryptedPreferenceProvider(
    private val sharedPreferences: SharedPreferences,
    private val dispatcher: CoroutineDispatcher
) : PreferenceProvider {

    override suspend fun hasKey(preferenceKey: PreferenceKey) = withContext(dispatcher) {
        sharedPreferences.contains(preferenceKey.key)
    }

    @SuppressLint("ApplySharedPref")
    override suspend fun putString(preferenceKey: PreferenceKey, value: String) = withContext(dispatcher) {
        val editor = sharedPreferences.edit()

        editor.putString(preferenceKey.key, value)

        editor.commit()

        Unit
    }

    override suspend fun getString(preferenceKey: PreferenceKey) = withContext(dispatcher) {
        sharedPreferences.getString(preferenceKey.key, null)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun observe(preferenceKey: PreferenceKey): Flow<String?> = callbackFlow<Unit> {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            // Callback on main thread
            trySend(Unit)
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)

        // Kickstart the emissions
        trySend(Unit)

        awaitClose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.flowOn(dispatcher)
        .map { getString(preferenceKey) }

    companion object {
        suspend fun new(context: Context, filename: String): PreferenceProvider {
            /*
             * Because of this line, we don't want multiple instances of this object created
             * because we don't clean up the thread afterwards.
             */
            val singleThreadedDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

            val mainKey = withContext(singleThreadedDispatcher) {
                @Suppress("BlockingMethodInNonBlockingContext")
                MasterKey.Builder(context).apply {
                    setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                }.build()
            }

            val sharedPreferences = withContext(singleThreadedDispatcher) {
                @Suppress("BlockingMethodInNonBlockingContext")
                EncryptedSharedPreferences.create(
                    context,
                    filename,
                    mainKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            }

            return EncryptedPreferenceProvider(sharedPreferences, singleThreadedDispatcher)
        }
    }
}
