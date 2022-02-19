package cash.z.ecc.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.FontRes
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.core.content.res.ResourcesCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cash.z.ecc.android.sdk.type.WalletBirthday
import cash.z.ecc.android.sdk.type.ZcashNetwork
import cash.z.ecc.sdk.model.PersistableWallet
import cash.z.ecc.sdk.model.SeedPhrase
import cash.z.ecc.sdk.model.ZecRequest
import cash.z.ecc.sdk.type.fromResources
import cash.z.ecc.ui.screen.backup.view.BackupWallet
import cash.z.ecc.ui.screen.backup.viewmodel.BackupViewModel
import cash.z.ecc.ui.screen.common.GradientSurface
import cash.z.ecc.ui.screen.home.view.Home
import cash.z.ecc.ui.screen.home.viewmodel.SecretState
import cash.z.ecc.ui.screen.home.viewmodel.WalletViewModel
import cash.z.ecc.ui.screen.onboarding.view.Onboarding
import cash.z.ecc.ui.screen.onboarding.viewmodel.OnboardingViewModel
import cash.z.ecc.ui.screen.profile.view.Profile
import cash.z.ecc.ui.screen.request.view.Request
import cash.z.ecc.ui.screen.restore.view.RestoreWallet
import cash.z.ecc.ui.screen.restore.viewmodel.CompleteWordSetState
import cash.z.ecc.ui.screen.restore.viewmodel.RestoreViewModel
import cash.z.ecc.ui.screen.seed.view.Seed
import cash.z.ecc.ui.screen.settings.view.Settings
import cash.z.ecc.ui.screen.wallet_address.view.WalletAddresses
import cash.z.ecc.ui.theme.ZcashTheme
import cash.z.ecc.ui.util.AndroidApiVersion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Suppress("TooManyFunctions")
class MainActivity : ComponentActivity() {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val walletViewModel by viewModels<WalletViewModel>()

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    lateinit var navControllerForTesting: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupSplashScreen()

        if (AndroidApiVersion.isAtLeastO) {
            setupUiContent()
        } else {
            lifecycleScope.launch {
                prefetchFontLegacy(applicationContext, R.font.rubik_medium)
                prefetchFontLegacy(applicationContext, R.font.rubik_regular)

                setupUiContent()
            }
        }
    }

    private fun setupSplashScreen() {
        val splashScreen = installSplashScreen()
        val start = SystemClock.elapsedRealtime().milliseconds

        splashScreen.setKeepVisibleCondition {
            if (SPLASH_SCREEN_DELAY > Duration.ZERO) {
                val now = SystemClock.elapsedRealtime().milliseconds

                // This delay is for debug purposes only; do not enable for production usage.
                if (now - start < SPLASH_SCREEN_DELAY) {
                    return@setKeepVisibleCondition true
                }
            }

            SecretState.Loading == walletViewModel.secretState.value
        }
    }

    private fun setupUiContent() {
        setContent {
            ZcashTheme {
                GradientSurface(
                    Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                ) {
                    when (val secretState = walletViewModel.secretState.collectAsState().value) {
                        SecretState.Loading -> {
                            // For now, keep displaying splash screen using condition above.
                            // In the future, we might consider displaying something different here.
                        }
                        SecretState.None -> {
                            WrapOnboarding()
                        }
                        is SecretState.NeedsBackup -> WrapBackup(secretState.persistableWallet)
                        is SecretState.Ready -> Navigation()
                    }
                }
            }
        }

        // Force collection to improve performance; sync can start happening while
        // the user is going through the backup flow. Don't use eager collection in the view model,
        // so that the collection is still tied to UI lifecycle.
        lifecycleScope.launch {
            walletViewModel.synchronizer.collect()
        }
    }

    @Composable
    private fun WrapOnboarding() {
        val onboardingViewModel by viewModels<OnboardingViewModel>()

        if (!onboardingViewModel.isImporting.collectAsState().value) {
            Onboarding(
                onboardingState = onboardingViewModel.onboardingState,
                onImportWallet = { onboardingViewModel.isImporting.value = true },
                onCreateWallet = {
                    walletViewModel.persistNewWallet()
                }
            )

            reportFullyDrawn()
        } else {
            WrapRestore()
        }
    }

    @Composable
    private fun WrapBackup(persistableWallet: PersistableWallet) {
        val backupViewModel by viewModels<BackupViewModel>()

        BackupWallet(
            persistableWallet, backupViewModel.backupState, backupViewModel.testChoices,
            onCopyToClipboard = {
                copyToClipboard(applicationContext, persistableWallet)
            }, onComplete = {
                walletViewModel.persistBackupComplete()
            }
        )
    }

    @Composable
    private fun WrapRestore() {
        val onboardingViewModel by viewModels<OnboardingViewModel>()
        val restoreViewModel by viewModels<RestoreViewModel>()

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
                        val clipboardManager = getSystemService(ClipboardManager::class.java)
                        return@RestoreWallet clipboardManager?.primaryClip?.toString()
                    },
                    onFinished = {
                        // Write the backup complete flag first, then the seed phrase.  That avoids the UI
                        // flickering to the backup screen.  Assume if a user is restoring from
                        // a backup, then the user has a valid backup.
                        walletViewModel.persistBackupComplete()

                        val network = ZcashNetwork.fromResources(application)
                        val restoredWallet = PersistableWallet(
                            network,
                            WalletBirthday(network.saplingActivationHeight),
                            SeedPhrase(restoreViewModel.userWordList.current.value)
                        )
                        walletViewModel.persistExistingWallet(restoredWallet)
                    }
                )
            }
        }
    }

    @Composable
    private fun Navigation() {
        val navController = rememberNavController().also {
            navControllerForTesting = it
        }

        NavHost(navController = navController, startDestination = NAV_HOME) {
            composable(NAV_HOME) {
                WrapHome(
                    goScan = {},
                    goProfile = { navController.navigate(NAV_PROFILE) },
                    goSend = {},
                    goRequest = { navController.navigate(NAV_REQUEST) }
                )
            }
            composable(NAV_PROFILE) {
                WrapProfile(
                    onBack = { navController.popBackStack() },
                    onAddressDetails = { navController.navigate(NAV_WALLET_ADDRESS_DETAILS) },
                    onAddressBook = { },
                    onSettings = { navController.navigate(NAV_SETTINGS) },
                    onCoinholderVote = { },
                    onSupport = {}
                )
            }
            composable(NAV_WALLET_ADDRESS_DETAILS) {
                WrapWalletAddresses(
                    goBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(NAV_SETTINGS) {
                WrapSettings(
                    goBack = {
                        navController.popBackStack()
                    },
                    goWalletBackup = {
                        navController.navigate(NAV_SEED)
                    }
                )
            }
            composable(NAV_SEED) {
                WrapSeed(
                    goBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(NAV_REQUEST) {
                WrapRequest(goBack = { navController.popBackStack() })
            }
        }
    }

    @Composable
    private fun WrapHome(
        goScan: () -> Unit,
        goProfile: () -> Unit,
        goSend: () -> Unit,
        goRequest: () -> Unit
    ) {
        val walletSnapshot = walletViewModel.walletSnapshot.collectAsState().value
        if (null == walletSnapshot) {
            // Display loading indicator
        } else {
            Home(
                walletSnapshot,
                walletViewModel.transactionSnapshot.collectAsState().value,
                goScan = goScan,
                goRequest = goRequest,
                goSend = goSend,
                goProfile = goProfile
            )

            reportFullyDrawn()
        }
    }

    @Composable
    @Suppress("LongParameterList")
    private fun WrapProfile(
        onBack: () -> Unit,
        onAddressDetails: () -> Unit,
        onAddressBook: () -> Unit,
        onSettings: () -> Unit,
        onCoinholderVote: () -> Unit,
        onSupport: () -> Unit
    ) {
        val walletAddresses = walletViewModel.addresses.collectAsState().value
        if (null == walletAddresses) {
            // Display loading indicator
        } else {
            Profile(
                walletAddresses.unified,
                onBack = onBack,
                onAddressDetails = onAddressDetails,
                onAddressBook = onAddressBook,
                onSettings = onSettings,
                onCoinholderVote = onCoinholderVote,
                onSupport = onSupport
            )
        }
    }

    @Composable
    private fun WrapWalletAddresses(
        goBack: () -> Unit,
    ) {
        val walletAddresses = walletViewModel.addresses.collectAsState().value
        if (null == walletAddresses) {
            // Display loading indicator
        } else {
            WalletAddresses(
                walletAddresses,
                goBack
            )
        }
    }

    @Composable
    private fun WrapSettings(
        goBack: () -> Unit,
        goWalletBackup: () -> Unit
    ) {
        val synchronizer = walletViewModel.synchronizer.collectAsState().value
        if (null == synchronizer) {
            // Display loading indicator
        } else {
            Settings(
                onBack = goBack,
                onBackupWallet = goWalletBackup,
                onRescanWallet = {
                    walletViewModel.rescanBlockchain()
                }, onWipeWallet = {
                    walletViewModel.wipeWallet()

                    // If wipe ever becomes an operation to also delete the seed, then we'll also need
                    // to do the following to clear any retained state from onboarding (only happens if
                    // occurring during same session as onboarding)
                    // onboardingViewModel.onboardingState.goToBeginning()
                    // onboardingViewModel.isImporting.value = false
                }
            )
        }
    }

    @Composable
    private fun WrapSeed(
        goBack: () -> Unit
    ) {
        val persistableWallet = run {
            val secretState = walletViewModel.secretState.collectAsState().value
            if (secretState is SecretState.Ready) {
                secretState.persistableWallet
            } else {
                null
            }
        }
        val synchronizer = walletViewModel.synchronizer.collectAsState().value
        if (null == synchronizer || null == persistableWallet) {
            // Display loading indicator
        } else {
            Seed(
                persistableWallet = persistableWallet,
                onBack = goBack,
                onCopyToClipboard = {
                    copyToClipboard(applicationContext, persistableWallet)
                }
            )
        }
    }

    @Composable
    private fun WrapRequest(
        goBack: () -> Unit
    ) {
        val walletAddresses = walletViewModel.addresses.collectAsState().value
        if (null == walletAddresses) {
            // Display loading indicator
        } else {
            Request(
                walletAddresses.unified,
                goBack = goBack,
                onCreateAndSend = {
                    val chooserIntent = Intent.createChooser(it.newShareIntent(applicationContext), null)

                    startActivity(chooserIntent)

                    goBack()
                },
            )
        }
    }

    companion object {
        @VisibleForTesting
        internal val SPLASH_SCREEN_DELAY = 0.seconds

        @VisibleForTesting
        const val NAV_HOME = "home"

        @VisibleForTesting
        const val NAV_PROFILE = "profile"

        @VisibleForTesting
        const val NAV_WALLET_ADDRESS_DETAILS = "wallet_address_details"

        @VisibleForTesting
        const val NAV_SETTINGS = "settings"

        @VisibleForTesting
        const val NAV_SEED = "seed"

        @VisibleForTesting
        const val NAV_REQUEST = "request"
    }
}

private fun copyToClipboard(context: Context, persistableWallet: PersistableWallet) {
    val clipboardManager = context.getSystemService(ClipboardManager::class.java)
    val data = ClipData.newPlainText(
        context.getString(R.string.new_wallet_clipboard_tag),
        persistableWallet.seedPhrase.joinToString()
    )
    clipboardManager.setPrimaryClip(data)
}

/**
 * Pre-fetches fonts on Android N (API 25) and below.
 */
/*
 * ResourcesCompat is used implicitly by Compose on older Android versions.
 * The backwards compatibility library performs disk IO and then
 * caches the results.  This moves that IO off the main thread, to prevent ANRs and
 * jank during app startup.
 */
private suspend fun prefetchFontLegacy(context: Context, @FontRes fontRes: Int) =
    withContext(Dispatchers.IO) {
        ResourcesCompat.getFont(context, fontRes)
    }

private fun ZecRequest.newShareIntent(context: Context) = Intent().apply {
    action = Intent.ACTION_SEND
    putExtra(Intent.EXTRA_TEXT, context.getString(R.string.request_template_format, toUri()))
    type = "text/plain"
}
