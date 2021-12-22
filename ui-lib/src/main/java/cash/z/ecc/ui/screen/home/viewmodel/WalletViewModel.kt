package cash.z.ecc.ui.screen.home.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cash.z.ecc.android.sdk.Synchronizer
import cash.z.ecc.android.sdk.block.CompactBlockProcessor
import cash.z.ecc.android.sdk.db.entity.PendingTransaction
import cash.z.ecc.android.sdk.db.entity.Transaction
import cash.z.ecc.android.sdk.db.entity.isMined
import cash.z.ecc.android.sdk.db.entity.isSubmitSuccess
import cash.z.ecc.android.sdk.type.WalletBalance
import cash.z.ecc.sdk.SynchronizerCompanion
import cash.z.ecc.sdk.model.PersistableWallet
import cash.z.ecc.ui.common.ANDROID_STATE_FLOW_TIMEOUT_MILLIS
import cash.z.ecc.ui.preference.EncryptedPreferenceKeys
import cash.z.ecc.ui.preference.EncryptedPreferenceSingleton
import cash.z.ecc.ui.preference.StandardPreferenceKeys
import cash.z.ecc.ui.preference.StandardPreferenceSingleton
import cash.z.ecc.ui.screen.home.model.WalletSnapshot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// To make this more multiplatform compatible, we need to remove the dependency on Context
// for loading the preferences.
class WalletViewModel(application: Application) : AndroidViewModel(application) {
    /*
     * Using the Mutex may be overkill, but it ensures that if multiple calls are accidentally made
     * that they have a consistent ordering.
     */
    private val persistWalletMutex = Mutex()

    /**
     * A flow of the user's stored wallet.  Null indicates that no wallet has been stored.
     */
    private val persistableWallet = flow {
        // EncryptedPreferenceSingleton.getInstance() is a suspending function, which is why we need
        // the flow builder to provide a coroutine context.
        val encryptedPreferenceProvider = EncryptedPreferenceSingleton.getInstance(application)

        emitAll(EncryptedPreferenceKeys.PERSISTABLE_WALLET.observe(encryptedPreferenceProvider))
    }

    /**
     * A flow of whether a backup of the user's wallet has been performed.
     */
    private val isBackupComplete = flow {
        val preferenceProvider = StandardPreferenceSingleton.getInstance(application)
        emitAll(StandardPreferenceKeys.IS_USER_BACKUP_COMPLETE.observe(preferenceProvider))
    }

    val secretState: StateFlow<SecretState> = persistableWallet
        .combine(isBackupComplete) { persistableWallet: PersistableWallet?, isBackupComplete: Boolean ->
            if (null == persistableWallet) {
                SecretState.None
            } else if (!isBackupComplete) {
                SecretState.NeedsBackup(persistableWallet)
            } else {
                SecretState.Ready(persistableWallet)
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(stopTimeoutMillis = ANDROID_STATE_FLOW_TIMEOUT_MILLIS),
            SecretState.Loading
        )

    // This will likely move to an application global, so that it can be referenced by WorkManager
    // for background synchronization
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val synchronizer: StateFlow<Synchronizer?> = secretState
        .filterIsInstance<SecretState.Ready>()
        .flatMapConcat {
            callbackFlow {
                val synchronizer = SynchronizerCompanion.load(application, it.persistableWallet)

                synchronizer.start(viewModelScope)

                trySend(synchronizer)
                awaitClose {
                    synchronizer.stop()
                }
            }
        }.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = ANDROID_STATE_FLOW_TIMEOUT_MILLIS),
            null
        )

    @OptIn(FlowPreview::class)
    val walletSnapshot: StateFlow<WalletSnapshot?> = synchronizer
        .filterNotNull()
        .flatMapConcat { it.toWalletSnapshot() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(stopTimeoutMillis = ANDROID_STATE_FLOW_TIMEOUT_MILLIS),
            null
        )

    // This is not the right API, because the transaction list could be very long and might need UI filtering
    @OptIn(FlowPreview::class)
    val transactionSnapshot: StateFlow<List<Transaction>> = synchronizer
        .filterNotNull()
        .flatMapConcat { it.toTransactions() }
        .stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = ANDROID_STATE_FLOW_TIMEOUT_MILLIS),
            emptyList()
        )

    /**
     * Creates a wallet asynchronously and then persists it.  Clients observe
     * [secretState] to see the side effects.  This would be used for a user creating a new wallet.
     */
    /*
     * Although waiting for the wallet to be written and then read back is slower, it is probably
     * safer because it 1. guarantees the wallet is written to disk and 2. has a single source of truth.
     */
    fun persistNewWallet() {
        val application = getApplication<Application>()

        viewModelScope.launch {
            val newWallet = PersistableWallet.new(application)
            persistExistingWallet(newWallet)
        }
    }

    /**
     * Persists a wallet asynchronously.  Clients observe [secretState]
     * to see the side effects.  This would be used for a user restoring a wallet from a backup.
     */
    fun persistExistingWallet(persistableWallet: PersistableWallet) {
        val application = getApplication<Application>()

        viewModelScope.launch {
            val preferenceProvider = EncryptedPreferenceSingleton.getInstance(application)
            persistWalletMutex.withLock {
                EncryptedPreferenceKeys.PERSISTABLE_WALLET.putValue(preferenceProvider, persistableWallet)
            }
        }
    }

    /**
     * Asynchronously notes that the user has completed the backup steps, which means the wallet
     * is ready to use.  Clients observe [secretState] to see the side effects.  This would be used
     * for a user creating a new wallet.
     */
    fun persistBackupComplete() {
        val application = getApplication<Application>()

        viewModelScope.launch {
            val preferenceProvider = StandardPreferenceSingleton.getInstance(application)

            // Use the Mutex here to avoid timing issues.  During wallet restore, persistBackupComplete()
            // is called prior to persistExistingWallet().  Although persistBackupComplete() should
            // complete quickly, it isn't guaranteed to complete before persistExistingWallet()
            // unless a mutex is used here.
            persistWalletMutex.withLock {
                StandardPreferenceKeys.IS_USER_BACKUP_COMPLETE.putValue(preferenceProvider, true)
            }
        }
    }
}

/**
 * Represents the state of the wallet secret.
 */
sealed class SecretState {
    object Loading : SecretState()
    object None : SecretState()
    class NeedsBackup(val persistableWallet: PersistableWallet) : SecretState()
    class Ready(val persistableWallet: PersistableWallet) : SecretState()
}

private fun Synchronizer.toWalletSnapshot() =
    combine(
        status, // 0
        processorInfo, // 1
        orchardBalances, // 2
        saplingBalances, // 3
        transparentBalances, // 4
        pendingTransactions.distinctUntilChanged() // 5
    ) { flows ->
        val unminedCount = (flows[5] as List<*>)
            .filterIsInstance(PendingTransaction::class.java)
            .count {
                it.isSubmitSuccess() && !it.isMined()
            }
        WalletSnapshot(
            status = flows[0] as Synchronizer.Status,
            processorInfo = flows[1] as CompactBlockProcessor.ProcessorInfo,
            orchardBalance = flows[2] as WalletBalance,
            saplingBalance = flows[3] as WalletBalance,
            transparentBalance = flows[4] as WalletBalance,
            unminedCount = unminedCount
        )
    }

private fun Synchronizer.toTransactions() =
    combine(
        clearedTransactions.distinctUntilChanged(),
        pendingTransactions.distinctUntilChanged(),
        sentTransactions.distinctUntilChanged(),
        receivedTransactions.distinctUntilChanged(),
    ) { cleared, pending, sent, received ->
        // TODO [#157]: Sort the transactions to show the most recent
        buildList<Transaction> {
            addAll(cleared)
            addAll(pending)
            addAll(sent)
            addAll(received)
        }
    }
