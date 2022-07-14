package co.electriccoin.zcash.ui.screen.home.view

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cash.z.ecc.android.sdk.db.entity.Transaction
import cash.z.ecc.sdk.ext.ui.model.FiatCurrencyConversionRateState
import cash.z.ecc.sdk.model.PercentDecimal
import co.electriccoin.zcash.crash.android.CrashReporter
import co.electriccoin.zcash.ui.R
import co.electriccoin.zcash.ui.design.MINIMAL_WEIGHT
import co.electriccoin.zcash.ui.design.component.Body
import co.electriccoin.zcash.ui.design.component.BodyWithFiatCurrencySymbol
import co.electriccoin.zcash.ui.design.component.GradientSurface
import co.electriccoin.zcash.ui.design.component.HeaderWithZecIcon
import co.electriccoin.zcash.ui.design.component.PrimaryButton
import co.electriccoin.zcash.ui.design.component.TertiaryButton
import co.electriccoin.zcash.ui.design.theme.ZcashTheme
import co.electriccoin.zcash.ui.fixture.WalletSnapshotFixture
import co.electriccoin.zcash.ui.screen.home.HomeTag
import co.electriccoin.zcash.ui.screen.home.model.WalletDisplayValues
import co.electriccoin.zcash.ui.screen.home.model.WalletSnapshot
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Preview
@Composable
fun ComposablePreview() {
    ZcashTheme(darkTheme = true) {
        GradientSurface {
            Home(
                WalletSnapshotFixture.new(),
                emptyList(),
                goScan = {},
                goProfile = {},
                goSend = {},
                goRequest = {},
                resetSdk = {},
                wipeEntireWallet = {},
                isDebugMenuEnabled = false,
                updateAvailable = false
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongParameterList")
@Composable
fun Home(
    walletSnapshot: WalletSnapshot,
    transactionHistory: List<Transaction>,
    goScan: () -> Unit,
    goProfile: () -> Unit,
    goSend: () -> Unit,
    goRequest: () -> Unit,
    resetSdk: () -> Unit,
    wipeEntireWallet: () -> Unit,
    isDebugMenuEnabled: Boolean,
    updateAvailable: Boolean
) {
    Scaffold(topBar = {
        HomeTopAppBar(isDebugMenuEnabled, resetSdk, wipeEntireWallet)
    }) { paddingValues ->
        HomeMainContent(
            paddingValues,
            walletSnapshot,
            transactionHistory,
            goScan = goScan,
            goProfile = goProfile,
            goSend = goSend,
            goRequest = goRequest,
            updateAvailable = updateAvailable
        )
    }
}

@Composable
private fun HomeTopAppBar(
    isDebugMenuEnabled: Boolean,
    resetSdk: () -> Unit,
    wipeEntireWallet: () -> Unit
) {
    SmallTopAppBar(
        title = { Text(text = stringResource(id = R.string.app_name)) },
        actions = {
            if (isDebugMenuEnabled) {
                DebugMenu(resetSdk, wipeEntireWallet)
            }
        }
    )
}

@OptIn(DelicateCoroutinesApi::class)
@Composable
private fun DebugMenu(resetSdk: () -> Unit, wipeEntireWallet: () -> Unit) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    IconButton(onClick = { expanded = true }) {
        Icon(Icons.Default.MoreVert, contentDescription = null)
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        DropdownMenuItem(
            text = { Text("Throw Uncaught Exception") },
            onClick = {
                GlobalScope.launch {
                    // Supposed to be generic, for manual debugging only
                    @Suppress("TooGenericExceptionThrown")
                    throw RuntimeException("Manually crashed from debug menu")
                }
            }
        )
        DropdownMenuItem(
            text = { Text("Report Caught Exception") },
            onClick = {
                // Eventually this shouldn't rely on the Android implementation, but rather an expect/actual
                // should be used at the crash API level.
                CrashReporter.reportCaughtException(RuntimeException("Manually caught exception from debug menu"))
                expanded = false
            }
        )
        DropdownMenuItem(
            text = { Text("Reset SDK") },
            onClick = {
                resetSdk()
                expanded = false
            }
        )
        DropdownMenuItem(
            text = { Text("Wipe entire wallet") },
            onClick = {
                wipeEntireWallet()
                expanded = false
            }
        )
    }
}

@Suppress("LongParameterList")
@Composable
private fun HomeMainContent(
    paddingValues: PaddingValues,
    walletSnapshot: WalletSnapshot,
    transactionHistory: List<Transaction>,
    goScan: () -> Unit,
    goProfile: () -> Unit,
    goSend: () -> Unit,
    goRequest: () -> Unit,
    updateAvailable: Boolean
) {
    Column(Modifier.verticalScroll(rememberScrollState())) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            IconButton(goScan) {
                Icon(
                    imageVector = Icons.Filled.QrCodeScanner,
                    contentDescription = stringResource(R.string.home_scan_content_description)
                )
            }
            Spacer(
                Modifier
                    .fillMaxWidth()
                    .weight(MINIMAL_WEIGHT)
            )
            IconButton(goProfile) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = stringResource(R.string.home_profile_content_description)
                )
            }
        }

        Status(walletSnapshot, updateAvailable)

        Spacer(modifier = Modifier.height(24.dp))

        PrimaryButton(onClick = goSend, text = stringResource(R.string.home_button_send))

        TertiaryButton(onClick = goRequest, text = stringResource(R.string.home_button_request))

        History(transactionHistory)
    }
}

@Composable
@Suppress("LongMethod", "MagicNumber")
private fun Status(
    walletSnapshot: WalletSnapshot,
    updateAvailable: Boolean
) {
    val configuration = LocalConfiguration.current
    val contentSizeRatioRatio = if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        0.45f
    } else {
        0.9f
    }

    // UI parts sizes
    val progressCircleStroke = 12.dp
    val progressCirclePadding = progressCircleStroke + 6.dp
    val contentPadding = progressCircleStroke + progressCirclePadding + 10.dp

    val walletDisplayValues = WalletDisplayValues.getNextValues(
        LocalContext.current,
        walletSnapshot,
        updateAvailable
    )

    // wrapper box
    Box(
        Modifier
            .fillMaxWidth()
            .testTag(HomeTag.STATUS_VIEWS),
        contentAlignment = Alignment.Center
    ) {
        // relatively sized box
        Box(
            modifier = Modifier
                .fillMaxWidth(contentSizeRatioRatio)
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            // progress circle
            if (walletDisplayValues.progress.decimal > PercentDecimal.ZERO_PERCENT.decimal) {
                CircularProgressIndicator(
                    progress = walletDisplayValues.progress.decimal,
                    color = Color.Gray,
                    strokeWidth = progressCircleStroke,
                    modifier = Modifier
                        .matchParentSize()
                        .padding(progressCirclePadding)
                        .testTag(HomeTag.PROGRESS)
                )
            }

            // texts
            Column(
                modifier = Modifier
                    .padding(contentPadding)
                    .wrapContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                if (walletDisplayValues.zecAmountText.isNotEmpty()) {
                    HeaderWithZecIcon(amount = walletDisplayValues.zecAmountText)
                }

                Spacer(modifier = Modifier.height(8.dp))

                when (walletDisplayValues.fiatCurrencyAmountState) {
                    is FiatCurrencyConversionRateState.Current -> {
                        BodyWithFiatCurrencySymbol(
                            amount = walletDisplayValues.fiatCurrencyAmountText
                        )
                    }
                    is FiatCurrencyConversionRateState.Stale -> {
                        // Note: we should show information about staleness too
                        BodyWithFiatCurrencySymbol(
                            amount = walletDisplayValues.fiatCurrencyAmountText
                        )
                    }
                    is FiatCurrencyConversionRateState.Unavailable -> {
                        Body(text = walletDisplayValues.fiatCurrencyAmountText)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (walletDisplayValues.statusText.isNotEmpty()) {
                    Body(
                        text = walletDisplayValues.statusText,
                        modifier = Modifier.testTag(HomeTag.SINGLE_LINE_TEXT)
                    )
                }
            }
        }
    }
}

@Composable
@Suppress("MagicNumber")
private fun History(transactionHistory: List<Transaction>) {
    if (transactionHistory.isEmpty()) {
        return
    }

    // here we need to use a fixed height to avoid nested columns vertical scrolling problem
    // we'll refactor this part to a dedicated bottom sheet later
    val historyPart = LocalConfiguration.current.screenHeightDp / 3

    LazyColumn(
        Modifier
            .fillMaxWidth()
            .height(historyPart.dp)
    ) {
        items(transactionHistory) {
            Text(it.toString())
        }
    }
}
