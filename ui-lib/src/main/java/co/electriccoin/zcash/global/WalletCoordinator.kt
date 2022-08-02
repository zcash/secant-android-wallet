package co.electriccoin.zcash.global

import android.content.Context
import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.toSeed
import cash.z.ecc.android.sdk.Initializer
import cash.z.ecc.android.sdk.Synchronizer
import cash.z.ecc.android.sdk.ext.onFirst
import cash.z.ecc.android.sdk.model.LightWalletEndpoint
import cash.z.ecc.android.sdk.tool.DerivationTool
import cash.z.ecc.android.sdk.type.UnifiedViewingKey
import cash.z.ecc.android.sdk.model.ZcashNetwork
import cash.z.ecc.android.sdk.model.defaultForNetwork
import cash.z.ecc.sdk.model.PersistableWallet
import cash.z.ecc.sdk.type.fromResources
import co.electriccoin.zcash.spackle.LazyWithArgument
import co.electriccoin.zcash.spackle.Twig
import co.electriccoin.zcash.ui.preference.EncryptedPreferenceKeys
import co.electriccoin.zcash.ui.preference.EncryptedPreferenceSingleton
import co.electriccoin.zcash.ui.preference.StandardPreferenceKeys
import co.electriccoin.zcash.ui.preference.StandardPreferenceSingleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.UUID

class WalletCoordinator(context: Context) {
    companion object {
        private val lazy = LazyWithArgument<Context, WalletCoordinator> { WalletCoordinator(it) }

        fun getInstance(context: Context) = lazy.getInstance(context)
    }

    private val applicationContext = context.applicationContext

    /*
     * We want a global scope that is independent of the lifecycles of either
     * WorkManager or the UI.
     */
    @OptIn(DelicateCoroutinesApi::class)
    private val walletScope = CoroutineScope(GlobalScope.coroutineContext + Dispatchers.Main)

    private val synchronizerMutex = Mutex()

    /**
     * A flow of the user's stored wallet.  Null indicates that no wallet has been stored.
     */
    val persistableWallet = flow {
        // EncryptedPreferenceSingleton.getInstance() is a suspending function, which is why we need
        // the flow builder to provide a coroutine context.
        val encryptedPreferenceProvider = EncryptedPreferenceSingleton.getInstance(applicationContext)

        emitAll(EncryptedPreferenceKeys.PERSISTABLE_WALLET.observe(encryptedPreferenceProvider))
    }

    private val lockoutMutex = Mutex()
    private val synchronizerLockoutId = MutableStateFlow<UUID?>(null)

    private sealed class InternalSynchronizerStatus {
        object NoWallet : InternalSynchronizerStatus()
        class Available(val synchronizer: cash.z.ecc.android.sdk.Synchronizer) : InternalSynchronizerStatus()
        class Lockout(val id: UUID) : InternalSynchronizerStatus()
    }

    private val synchronizerOrLockoutId: Flow<Flow<InternalSynchronizerStatus>> = persistableWallet
        .combine(synchronizerLockoutId) { persistableWallet: PersistableWallet?, lockoutId: UUID? ->
            if (null != lockoutId) { // this one needs to come first
                flowOf(InternalSynchronizerStatus.Lockout(lockoutId))
            } else if (null == persistableWallet) {
                flowOf(InternalSynchronizerStatus.NoWallet)
            } else {
                callbackFlow<InternalSynchronizerStatus.Available> {
                    val initializer = Initializer.new(context, persistableWallet.toConfig())
                    val synchronizer = synchronizerMutex.withLock {
                        val synchronizer = Synchronizer.new(initializer)
                        synchronizer.start(walletScope)
                    }

                    trySend(InternalSynchronizerStatus.Available(synchronizer))
                    awaitClose {
                        Twig.info { "Closing flow and stopping synchronizer" }
                        synchronizer.stop()
                    }
                }
            }
        }

    /**
     * Synchronizer for the Zcash SDK. Emits null until a wallet secret is persisted.
     *
     * Note that this synchronizer is closed as soon as it stops being collected.  For UI use
     * cases, see [WalletViewModel].
     */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val synchronizer: StateFlow<Synchronizer?> = synchronizerOrLockoutId
        .flatMapLatest {
            it
        }
        .map {
            when (it) {
                is InternalSynchronizerStatus.Available -> it.synchronizer
                is InternalSynchronizerStatus.Lockout -> null
                InternalSynchronizerStatus.NoWallet -> null
            }
        }
        .stateIn(
            walletScope,
            SharingStarted.WhileSubscribed(),
            null
        )

    /**
     * Rescans the blockchain.
     *
     * In order for a rescan to occur, the synchronizer must be loaded already
     * which would happen if the UI is collecting it.
     *
     * @return True if the rescan was performed and false if the rescan was not performed.
     */
    suspend fun rescanBlockchain(): Boolean {
        synchronizerMutex.withLock {
            synchronizer.value?.let {
                it.latestBirthdayHeight?.let { height ->
                    it.rewindToNearestHeight(height, true)
                    return true
                }
            }
        }

        return false
    }

    /**
     * Resets persisted data in the SDK, but preserves the wallet secret.  This will cause the
     * synchronizer to emit a new instance.
     */
    @OptIn(FlowPreview::class)
    fun resetSdk() {
        walletScope.launch {
            lockoutMutex.withLock {
                val lockoutId = UUID.randomUUID()
                synchronizerLockoutId.value = lockoutId

                synchronizerOrLockoutId
                    .flatMapConcat { it }
                    .filterIsInstance<InternalSynchronizerStatus.Lockout>()
                    .filter { it.id == lockoutId }
                    .onFirst {
                        synchronizerMutex.withLock {
                            val didDelete = Initializer.erase(
                                applicationContext,
                                ZcashNetwork.fromResources(applicationContext)
                            )

                            Twig.info { "SDK erase result: $didDelete" }
                        }
                    }

                synchronizerLockoutId.value = null
            }
        }
    }

    /**
     * Wipes the wallet.  Will cause the app-wide synchronizer to be reset with a new instance.
     */
    @OptIn(FlowPreview::class)
    fun wipeEntireWallet() {
        walletScope.launch {
            lockoutMutex.withLock {
                val lockoutId = UUID.randomUUID()
                synchronizerLockoutId.value = lockoutId

                synchronizerOrLockoutId
                    .flatMapConcat { it }
                    .filterIsInstance<InternalSynchronizerStatus.Lockout>()
                    .filter { it.id == lockoutId }
                    .onFirst {
                        // Note that clearing the data here is non-atomic since multiple files must be modified

                        EncryptedPreferenceSingleton.getInstance(applicationContext).also { provider ->
                            EncryptedPreferenceKeys.PERSISTABLE_WALLET.putValue(provider, null)
                        }

                        StandardPreferenceSingleton.getInstance(applicationContext).also { provider ->
                            StandardPreferenceKeys.IS_USER_BACKUP_COMPLETE.putValue(provider, false)
                        }

                        synchronizerMutex.withLock {
                            val didDelete = Initializer.erase(
                                applicationContext,
                                ZcashNetwork.fromResources(applicationContext)
                            )
                            Twig.info { "SDK erase result: $didDelete" }
                        }

                        synchronizerLockoutId.value = null
                    }
            }
        }
    }
}

private suspend fun PersistableWallet.deriveViewingKey(): UnifiedViewingKey {
    // Dispatcher needed because SecureRandom is loaded, which is slow and performs IO
    // https://github.com/zcash/kotlin-bip39/issues/13
    val bip39Seed = withContext(Dispatchers.IO) {
        Mnemonics.MnemonicCode(seedPhrase.joinToString()).toSeed()
    }

    return DerivationTool.deriveUnifiedViewingKeys(bip39Seed, network)[0]
}

private suspend fun PersistableWallet.toConfig(): Initializer.Config {
    val network = network
    val vk = deriveViewingKey()

    return Initializer.Config {
        it.importWallet(vk, birthday, network, LightWalletEndpoint.defaultForNetwork(network))
    }
}
