package cash.z.ecc.ui.screen.seed.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import cash.z.ecc.sdk.fixture.PersistableWalletFixture
import cash.z.ecc.sdk.model.PersistableWallet
import cash.z.ecc.ui.R
import cash.z.ecc.ui.screen.common.Body
import cash.z.ecc.ui.screen.common.ChipGrid
import cash.z.ecc.ui.screen.common.GradientSurface
import cash.z.ecc.ui.screen.common.Header
import cash.z.ecc.ui.screen.common.TertiaryButton
import cash.z.ecc.ui.theme.ZcashTheme

@Preview("Seed")
@Composable
fun PreviewSeed() {
    ZcashTheme(darkTheme = true) {
        GradientSurface {
            Seed(
                persistableWallet = PersistableWalletFixture.new(),
                onBack = {},
                onCopyToClipboard = {}
            )
        }
    }
}

/*
 * Note we have some things to determine regarding locking of the secrets for persistableWallet
 * (e.g. seed phrase and spending keys) which should require additional authorization to view.
 */
@Composable
fun Seed(
    persistableWallet: PersistableWallet,
    onBack: () -> Unit,
    onCopyToClipboard: () -> Unit
) {
    Scaffold(topBar = {
        SeedTopAppBar(onBack = onBack)
    }) {
        SeedMainContent(persistableWallet = persistableWallet, onCopyToClipboard = onCopyToClipboard)
    }
}

@Composable
private fun SeedTopAppBar(onBack: () -> Unit) {
    TopAppBar(
        title = { Text(text = stringResource(id = R.string.seed_title)) },
        navigationIcon = {
            IconButton(
                onClick = onBack
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.seed_back_content_description)
                )
            }
        }
    )
}

@Composable
private fun SeedMainContent(
    persistableWallet: PersistableWallet,
    onCopyToClipboard: () -> Unit
) {
    Column(Modifier.verticalScroll(rememberScrollState())) {
        Header(stringResource(R.string.seed_header))
        Body(stringResource(R.string.seed_body))

        ChipGrid(persistableWallet.seedPhrase.split)

        TertiaryButton(onClick = onCopyToClipboard, text = stringResource(R.string.seed_copy))
    }
}