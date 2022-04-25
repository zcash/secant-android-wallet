@file:Suppress("PackageNaming")
package co.electriccoin.zcash.ui.screen.update_available.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import co.electriccoin.zcash.ui.R
import co.electriccoin.zcash.ui.design.component.Body
import co.electriccoin.zcash.ui.design.component.GradientSurface
import co.electriccoin.zcash.ui.design.component.PrimaryButton
import co.electriccoin.zcash.ui.design.component.Reference
import co.electriccoin.zcash.ui.design.component.TertiaryButton
import co.electriccoin.zcash.ui.design.theme.ZcashTheme
import co.electriccoin.zcash.ui.fixture.UpdateInfoFixture
import co.electriccoin.zcash.ui.screen.update_available.model.UpdateInfo
import co.electriccoin.zcash.ui.screen.update_available.util.PlayStoreUtil

const val REFERENCE_TAG: String = "link_to_play_store"

@Preview("UpdateAvailable")
@Composable
fun PreviewUpdateAvailable() {
    ZcashTheme(darkTheme = true) {
        GradientSurface {
            UpdateAvailable(
                UpdateInfoFixture.new(appUpdateInfo = null),
                onDownload = {},
                onLater = {}
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateAvailable(
    updateInfo: UpdateInfo,
    onDownload: () -> Unit,
    onLater: () -> Unit
) {
    Scaffold(
        topBar = {
            UpdateAvailableTopAppBar(updateInfo)
        },
        bottomBar = {
            UpdateAvailableBottomAppBar(
                updateInfo,
                onDownload,
                onLater
            )
        }
    ) {
        UpdateAvailableContent()
    }
}

@Composable
private fun UpdateAvailableTopAppBar(updateInfo: UpdateInfo) {
    SmallTopAppBar(
        title = {
            Text(
                text = stringResource(
                    updateInfo.isForce.let { force ->
                        if (force)
                            R.string.update_available_critical_header
                        else
                            R.string.update_available_header
                    }
                )
            )
        },
    )
}

@Composable
private fun UpdateAvailableBottomAppBar(
    updateInfo: UpdateInfo,
    onDownload: () -> Unit,
    onLater: () -> Unit
) {
    Column {
        PrimaryButton(
            onClick = onDownload,
            text = stringResource(R.string.update_available_download_button),
            modifier = Modifier.fillMaxWidth()
        )

        TertiaryButton(
            onClick = onLater,
            text = stringResource(
                updateInfo.isForce.let { force ->
                    if (force)
                        R.string.update_available_later_disabled_button
                    else
                        R.string.update_available_later_enabled_button
                }
            ),
            modifier = Modifier.fillMaxWidth(),
            enabled = updateInfo.isForce
        )
    }
}

@Composable
private fun UpdateAvailableContent() {
    val context = LocalContext.current

    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // TODO [#17]: This suppression and magic number will get replaced once we have real assets
        @Suppress("MagicNumber")
        Image(
            painter = painterResource(id = R.drawable.update_available_main_graphic),
            contentDescription = stringResource(id = R.string.update_available_image_content_description),
            Modifier.fillMaxSize(0.50f)
        )

        Body(
            text = stringResource(id = R.string.update_available_description),
            modifier = Modifier
                .wrapContentHeight()
                .align(Alignment.CenterHorizontally)
        )

        Reference(
            text = stringResource(id = R.string.update_available_link_text),
            tag = REFERENCE_TAG,
            link = stringResource(id = R.string.update_available_link),
            modifier = Modifier
                .wrapContentHeight()
                .align(Alignment.CenterHorizontally),
            onClick = {
                val storeIntent = PlayStoreUtil.newActivityIntent(context)
                context.startActivity(storeIntent)
            }
        )
    }
}
