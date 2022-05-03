package co.electriccoin.zcash.ui.screen.about

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import co.electriccoin.zcash.ui.MainActivity
import co.electriccoin.zcash.ui.screen.about.model.VersionInfo
import co.electriccoin.zcash.ui.screen.about.view.About

@Composable
internal fun MainActivity.WrapAbout(
    goBack: () -> Unit
) {
    WrapAbout(this, goBack)
}

@Composable
internal fun WrapAbout(
    activity: ComponentActivity,
    goBack: () -> Unit
) {
    val packageInfo = activity.packageManager.getPackageInfo(activity.packageName, 0)

    About(VersionInfo.new(packageInfo), goBack)
}
