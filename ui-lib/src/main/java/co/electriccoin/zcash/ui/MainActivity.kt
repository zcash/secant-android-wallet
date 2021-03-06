package co.electriccoin.zcash.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cash.z.ecc.sdk.model.ZecRequest
import cash.z.ecc.sdk.send
import co.electriccoin.zcash.ui.design.compat.FontCompat
import co.electriccoin.zcash.ui.design.component.ConfigurationOverride
import co.electriccoin.zcash.ui.design.component.GradientSurface
import co.electriccoin.zcash.ui.design.component.Override
import co.electriccoin.zcash.ui.design.theme.ZcashTheme
import co.electriccoin.zcash.ui.screen.about.WrapAbout
import co.electriccoin.zcash.ui.screen.address.view.WalletAddresses
import co.electriccoin.zcash.ui.screen.backup.WrapBackup
import co.electriccoin.zcash.ui.screen.backup.copyToClipboard
import co.electriccoin.zcash.ui.screen.home.WrapHome
import co.electriccoin.zcash.ui.screen.home.model.spendableBalance
import co.electriccoin.zcash.ui.screen.home.viewmodel.CheckUpdateViewModel
import co.electriccoin.zcash.ui.screen.home.viewmodel.SecretState
import co.electriccoin.zcash.ui.screen.home.viewmodel.WalletViewModel
import co.electriccoin.zcash.ui.screen.onboarding.WrapOnboarding
import co.electriccoin.zcash.ui.screen.profile.WrapProfile
import co.electriccoin.zcash.ui.screen.request.view.Request
import co.electriccoin.zcash.ui.screen.scan.WrapScan
import co.electriccoin.zcash.ui.screen.seed.view.Seed
import co.electriccoin.zcash.ui.screen.send.view.Send
import co.electriccoin.zcash.ui.screen.settings.WrapSettings
import co.electriccoin.zcash.ui.screen.support.WrapSupport
import co.electriccoin.zcash.ui.screen.update.AppUpdateCheckerImp
import co.electriccoin.zcash.ui.screen.update.WrapUpdate
import co.electriccoin.zcash.ui.screen.update.model.UpdateState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Suppress("TooManyFunctions")
class MainActivity : ComponentActivity() {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val walletViewModel by viewModels<WalletViewModel>()

    // TODO [#382]: https://github.com/zcash/secant-android-wallet/issues/382
    // TODO [#403]: https://github.com/zcash/secant-android-wallet/issues/403
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val checkUpdateViewModel by viewModels<CheckUpdateViewModel> {
        CheckUpdateViewModel.CheckUpdateViewModelFactory(
            application,
            AppUpdateCheckerImp.new()
        )
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    lateinit var navControllerForTesting: NavHostController

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val configurationOverrideFlow = MutableStateFlow<ConfigurationOverride?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupSplashScreen()

        if (FontCompat.isFontPrefetchNeeded()) {
            lifecycleScope.launch {
                FontCompat.prefetchFontsLegacy(applicationContext)
                setupUiContent()
            }
        } else {
            setupUiContent()
        }
    }

    private fun setupSplashScreen() {
        val splashScreen = installSplashScreen()
        val start = SystemClock.elapsedRealtime().milliseconds

        splashScreen.setKeepOnScreenCondition {
            if (SPLASH_SCREEN_DELAY > Duration.ZERO) {
                val now = SystemClock.elapsedRealtime().milliseconds

                // This delay is for debug purposes only; do not enable for production usage.
                if (now - start < SPLASH_SCREEN_DELAY) {
                    return@setKeepOnScreenCondition true
                }
            }

            SecretState.Loading == walletViewModel.secretState.value
        }
    }

    private fun setupUiContent() {
        setContent {
            Override(configurationOverrideFlow) {
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
                            is SecretState.NeedsBackup -> WrapBackup(
                                secretState.persistableWallet,
                                onBackupComplete = { walletViewModel.persistBackupComplete() }
                            )
                            is SecretState.Ready -> Navigation()
                        }
                    }
                }
            }
        }

        // Force collection to improve performance; sync can start happening while
        // the user is going through the backup flow. Don't use eager collection in the view model,
        // so that the collection is still tied to UI lifecycle.
        lifecycleScope.launch {
            walletViewModel.synchronizer.collect {
            }
        }
    }

    @Suppress("LongMethod")
    @Composable
    @SuppressWarnings("LongMethod")
    private fun Navigation() {
        val navController = rememberNavController().also {
            // This suppress is necessary, as this is how we set up the nav controller for tests.
            @SuppressLint("RestrictedApi")
            navControllerForTesting = it
        }

        NavHost(navController = navController, startDestination = NAV_HOME) {
            composable(NAV_HOME) {
                WrapHome(
                    goScan = { navController.navigateJustOnce(NAV_SCAN) },
                    goProfile = { navController.navigateJustOnce(NAV_PROFILE) },
                    goSend = { navController.navigateJustOnce(NAV_SEND) },
                    goRequest = { navController.navigateJustOnce(NAV_REQUEST) }
                )

                WrapCheckForUpdate()
            }
            composable(NAV_PROFILE) {
                WrapProfile(
                    onBack = { navController.popBackStackJustOnce(NAV_PROFILE) },
                    onAddressDetails = { navController.navigateJustOnce(NAV_WALLET_ADDRESS_DETAILS) },
                    onAddressBook = { },
                    onSettings = { navController.navigateJustOnce(NAV_SETTINGS) },
                    onCoinholderVote = { },
                    onSupport = { navController.navigateJustOnce(NAV_SUPPORT) },
                    onAbout = { navController.navigateJustOnce(NAV_ABOUT) }
                )
            }
            composable(NAV_WALLET_ADDRESS_DETAILS) {
                WrapWalletAddresses(
                    goBack = {
                        navController.popBackStackJustOnce(NAV_WALLET_ADDRESS_DETAILS)
                    }
                )
            }
            composable(NAV_SETTINGS) {
                WrapSettings(
                    goBack = {
                        navController.popBackStackJustOnce(NAV_SETTINGS)
                    },
                    goWalletBackup = {
                        navController.navigateJustOnce(NAV_SEED)
                    }
                )
            }
            composable(NAV_SEED) {
                WrapSeed(
                    goBack = {
                        navController.popBackStackJustOnce(NAV_SEED)
                    }
                )
            }
            composable(NAV_REQUEST) {
                WrapRequest(goBack = { navController.popBackStackJustOnce(NAV_REQUEST) })
            }
            composable(NAV_SEND) {
                WrapSend(goBack = { navController.popBackStackJustOnce(NAV_SEND) })
            }
            composable(NAV_SUPPORT) {
                // Pop back stack won't be right if we deep link into support
                WrapSupport(goBack = { navController.popBackStackJustOnce(NAV_SUPPORT) })
            }
            composable(NAV_ABOUT) {
                WrapAbout(goBack = { navController.popBackStackJustOnce(NAV_ABOUT) })
            }
            composable(NAV_SCAN) {
                WrapScanValidator(
                    onScanValid = {
                        // TODO [#449] https://github.com/zcash/secant-android-wallet/issues/449
                        navController.navigateJustOnce(NAV_SEND) {
                            popUpTo(NAV_HOME) { inclusive = false }
                        }
                    },
                    goBack = { navController.popBackStackJustOnce(NAV_SCAN) }
                )
            }
        }
    }

    @Composable
    private fun WrapScanValidator(
        onScanValid: (address: String) -> Unit,
        goBack: () -> Unit
    ) {
        val synchronizer = walletViewModel.synchronizer.collectAsState().value
        if (synchronizer == null) {
            // Display loading indicator
        } else {
            WrapScan(
                onScanDone = { result ->
                    lifecycleScope.launch {
                        val isAddressValid = !synchronizer.validateAddress(result).isNotValid
                        if (isAddressValid) {
                            onScanValid(result)
                        }
                    }
                },
                goBack = goBack
            )
        }
    }

    @Composable
    private fun WrapCheckForUpdate() {
        val updateInfo = checkUpdateViewModel.updateInfo.collectAsState().value

        updateInfo?.let {
            if (it.appUpdateInfo != null && it.state == UpdateState.Prepared) {
                WrapUpdate(updateInfo)
            }
        }

        // Check for an app update asynchronously. We create an effect that matches the activity
        // lifecycle. If the wrapping compose recomposes, the check shouldn't run again.
        LaunchedEffect(true) {
            checkUpdateViewModel.checkForAppUpdate()
        }
    }

    @Composable
    private fun WrapWalletAddresses(
        goBack: () -> Unit
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
                }
            )
        }
    }

    @Composable
    private fun WrapSend(
        goBack: () -> Unit
    ) {
        val synchronizer = walletViewModel.synchronizer.collectAsState().value
        val spendableBalance = walletViewModel.walletSnapshot.collectAsState().value?.spendableBalance()
        val spendingKey = walletViewModel.spendingKey.collectAsState().value
        if (null == synchronizer || null == spendableBalance || null == spendingKey) {
            // Display loading indicator
        } else {
            Send(
                mySpendableBalance = spendableBalance,
                goBack = goBack,
                onCreateAndSend = {
                    synchronizer.send(spendingKey, it)

                    goBack()
                }
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

        @VisibleForTesting
        const val NAV_SEND = "send"

        @VisibleForTesting
        const val NAV_SUPPORT = "support"

        @VisibleForTesting
        const val NAV_ABOUT = "about"

        @VisibleForTesting
        const val NAV_SCAN = "scan"
    }
}

private fun ZecRequest.newShareIntent(context: Context) = runBlocking {
    Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, context.getString(R.string.request_template_format, toUri()))
        type = "text/plain"
    }
}

private fun NavHostController.navigateJustOnce(
    route: String,
    navOptionsBuilder: (NavOptionsBuilder.() -> Unit)? = null
) {
    if (currentDestination?.route == route) {
        return
    }

    if (navOptionsBuilder != null) {
        navigate(route, navOptionsBuilder)
    } else {
        navigate(route)
    }
}

/**
 * Pops up the current screen from the back stack. Parameter currentRouteToBePopped is meant to be
 * set only to the current screen so we can easily debounce multiple screen popping from the back stack.
 *
 * @param currentRouteToBePopped current screen which should be popped up.
 */
private fun NavHostController.popBackStackJustOnce(currentRouteToBePopped: String) {
    if (currentDestination?.route != currentRouteToBePopped) {
        return
    }
    popBackStack()
}
