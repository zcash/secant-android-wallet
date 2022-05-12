package co.electriccoin.zcash.ui.screen.update.view

import android.app.Activity
import android.content.Context
import androidx.activity.ComponentActivity
import co.electriccoin.zcash.ui.screen.update.AppUpdateChecker
import co.electriccoin.zcash.ui.screen.update.fixture.UpdateInfoFixture
import co.electriccoin.zcash.ui.screen.update.model.UpdateInfo
import co.electriccoin.zcash.ui.screen.update.model.UpdateState
import co.electriccoin.zcash.util.VersionCodeCompat
import co.electriccoin.zcash.util.myPackageInfo
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.testing.FakeAppUpdateManager
import com.google.android.play.core.install.model.AppUpdateType
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

// MANUAL_IN_APP_UPDATE_TEST (use instead of AppUpdateCheckerImp. Remove this class after test)
@Suppress("MagicNumber")
class AppUpdateCheckerTest private constructor() : AppUpdateChecker {

    companion object {
        private const val DEFAULT_STALENESS_DAYS = 3

        fun new() = AppUpdateCheckerTest()
    }

    override val stalenessDays = DEFAULT_STALENESS_DAYS

    override fun checkForUpdateAvailability(
        context: Context
    ): Flow<UpdateInfo> = callbackFlow {
        delay(2000)
        val fakeAppUpdateManager = FakeAppUpdateManager(context.applicationContext).also {
            it.setClientVersionStalenessDays(stalenessDays)
            it.setUpdateAvailable(
                VersionCodeCompat.getVersionCode(context.myPackageInfo(0)).toInt(),
                AppUpdateType.IMMEDIATE
            )
            // MANUAL_IN_APP_UPDATE_TEST - change this value from 0-5 to test force/available update types
            it.setUpdatePriority(5)
        }

        val appUpdateInfoTask = fakeAppUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnCompleteListener { infoTask ->
            emitResult(this, infoTask.result)
        }

        awaitClose {
            // No resources to release
        }
    }

    private fun emitResult(producerScope: ProducerScope<UpdateInfo>, info: AppUpdateInfo) {
        producerScope.trySend(
            UpdateInfoFixture.new(
                getPriority(info.updatePriority()),
                isHighPriority(info.updatePriority()),
                info,
                UpdateState.Prepared
            )
        )
    }

    override fun startUpdate(
        activity: ComponentActivity,
        appUpdateInfo: AppUpdateInfo
    ): Flow<Int> = callbackFlow {
        delay(3000)
        trySend(Activity.RESULT_OK)
        awaitClose {
            // No resources to release
        }
    }
}