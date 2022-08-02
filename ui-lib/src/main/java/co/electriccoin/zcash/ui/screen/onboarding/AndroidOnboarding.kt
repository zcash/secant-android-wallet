@file:Suppress("ktlint:filename")

package co.electriccoin.zcash.ui.screen.onboarding

import android.content.ClipboardManager
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import cash.z.ecc.android.sdk.model.ZcashNetwork
import cash.z.ecc.sdk.fixture.SeedPhraseFixture
import cash.z.ecc.sdk.model.PersistableWallet
import cash.z.ecc.sdk.model.SeedPhrase
import cash.z.ecc.sdk.type.fromResources
import co.electriccoin.zcash.spackle.EmulatorWtfUtil
import co.electriccoin.zcash.spackle.FirebaseTestLabUtil
import co.electriccoin.zcash.ui.BuildConfig
import co.electriccoin.zcash.ui.MainActivity
import co.electriccoin.zcash.ui.screen.home.viewmodel.WalletViewModel
import co.electriccoin.zcash.ui.screen.onboarding.view.Onboarding
import co.electriccoin.zcash.ui.screen.onboarding.viewmodel.OnboardingViewModel
import co.electriccoin.zcash.ui.screen.restore.view.RestoreWallet
import co.electriccoin.zcash.ui.screen.restore.viewmodel.CompleteWordSetState
import co.electriccoin.zcash.ui.screen.restore.viewmodel.RestoreViewModel

@Composable
internal fun MainActivity.WrapOnboarding() {
    WrapOnboarding(this)
}

@Composable
internal fun WrapOnboarding(
    activity: ComponentActivity
) {
    val walletViewModel by activity.viewModels<WalletViewModel>()
    val onboardingViewModel by activity.viewModels<OnboardingViewModel>()

    val applicationContext = LocalContext.current.applicationContext

    // We might eventually want to check the debuggable property of the manifest instead
    // of relying on BuildConfig.
    val isDebugMenuEnabled = BuildConfig.DEBUG &&
        !FirebaseTestLabUtil.isFirebaseTestLab(applicationContext) &&
        !EmulatorWtfUtil.isEmulatorWtf(applicationContext)

    // TODO [#383]: https://github.com/zcash/secant-android-wallet/issues/383
    if (!onboardingViewModel.isImporting.collectAsState().value) {
        Onboarding(
            onboardingState = onboardingViewModel.onboardingState,
            isDebugMenuEnabled = isDebugMenuEnabled,
            onImportWallet = {
                // In the case of the app currently being messed with by the robo test runner on
                // Firebase Test Lab or Google Play pre-launch report, we want to skip creating
                // a new or restoring an existing wallet screens by persisting an existing wallet
                // with a mock seed.
                if (FirebaseTestLabUtil.isFirebaseTestLab(applicationContext)) {
                    persistExistingWalletWithSeedPhrase(
                        applicationContext,
                        walletViewModel,
                        SeedPhraseFixture.new()
                    )
                    return@Onboarding
                }

                onboardingViewModel.isImporting.value = true
            },
            onCreateWallet = {
                if (FirebaseTestLabUtil.isFirebaseTestLab(applicationContext)) {
                    persistExistingWalletWithSeedPhrase(
                        applicationContext,
                        walletViewModel,
                        SeedPhraseFixture.new()
                    )
                    return@Onboarding
                }

                walletViewModel.persistNewWallet()
            },
            onFixtureWallet = {
                persistExistingWalletWithSeedPhrase(
                    applicationContext,
                    walletViewModel,
                    SeedPhraseFixture.new()
                )
            }
        )

        activity.reportFullyDrawn()
    } else {
        WrapRestore(activity)
    }
}

@Composable
private fun WrapRestore(activity: ComponentActivity) {
    val walletViewModel by activity.viewModels<WalletViewModel>()
    val onboardingViewModel by activity.viewModels<OnboardingViewModel>()
    val restoreViewModel by activity.viewModels<RestoreViewModel>()

    val applicationContext = LocalContext.current.applicationContext

    when (val completeWordList = restoreViewModel.completeWordList.collectAsState().value) {
        CompleteWordSetState.Loading -> {
            // Although it might perform IO, it should be relatively fast.
            // Consider whether to display indeterminate progress here.
            // Another option would be to go straight to the restore screen with autocomplete
            // disabled for a few milliseconds.  Users would probably never notice due to the
            // time it takes to re-orient on the new screen, unless users were doing this
            // on a daily basis and become very proficient at our UI.  The Therac-25 has
            // historical precedent on how that could cause problems.
        }
        is CompleteWordSetState.Loaded -> {
            RestoreWallet(
                completeWordList.list,
                restoreViewModel.userWordList,
                onBack = { onboardingViewModel.isImporting.value = false },
                paste = {
                    val clipboardManager = applicationContext.getSystemService(ClipboardManager::class.java)
                    return@RestoreWallet clipboardManager?.primaryClip?.toString()
                },
                onFinished = {
                    persistExistingWalletWithSeedPhrase(
                        applicationContext,
                        walletViewModel,
                        SeedPhrase(restoreViewModel.userWordList.current.value)
                    )
                }
            )
        }
    }
}

/**
 * Persists existing wallet together with the backup complete flag to disk. Be aware of that, it
 * triggers navigation changes, as we observe the WalletViewModel.secretState.
 *
 * Write the backup complete flag first, then the seed phrase. That avoids the UI flickering to
 * the backup screen. Assume if a user is restoring from a backup, then the user has a valid backup.
 *
 * @param seedPhrase to be persisted along with the wallet object
 */
private fun persistExistingWalletWithSeedPhrase(
    context: Context,
    walletViewModel: WalletViewModel,
    seedPhrase: SeedPhrase
) {
    walletViewModel.persistBackupComplete()

    val network = ZcashNetwork.fromResources(context)
    val restoredWallet = PersistableWallet(
        network,
        null,
        seedPhrase
    )
    walletViewModel.persistExistingWallet(restoredWallet)
}
