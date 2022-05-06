package co.electriccoin.zcash.ui.screen.update_available.integration

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.viewModelScope
import androidx.test.filters.MediumTest
import co.electriccoin.zcash.ui.screen.update_available.AppUpdateChecker
import co.electriccoin.zcash.ui.screen.update_available.TestUpdateAvailableActivity
import co.electriccoin.zcash.ui.screen.update_available.fixture.UpdateInfoFixture
import co.electriccoin.zcash.ui.screen.update_available.model.UpdateInfo
import co.electriccoin.zcash.ui.screen.update_available.model.UpdateState
import co.electriccoin.zcash.ui.screen.update_available.view.AppUpdateCheckerMock
import co.electriccoin.zcash.ui.screen.update_available.viewmodel.UpdateAvailableViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@ExperimentalCoroutinesApi
class UpdateAvailableViewModelTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<TestUpdateAvailableActivity>()

    private lateinit var viewModel: UpdateAvailableViewModel
    private lateinit var checker: AppUpdateCheckerMock
    private lateinit var initialUpdateInfo: UpdateInfo

    @Before
    fun setup() {
        checker = AppUpdateCheckerMock.new()

        initialUpdateInfo = UpdateInfoFixture.new(
            appUpdateInfo = null,
            state = UpdateState.Prepared,
            priority = AppUpdateChecker.Priority.LOW,
            force = false
        )

        viewModel = UpdateAvailableViewModel(
            composeTestRule.activity.application,
            initialUpdateInfo,
            checker
        )
    }

    @After
    fun cleanup() {
        viewModel.viewModelScope.cancel()
    }

    @Test
    @MediumTest
    fun validate_result_of_check_for_app_update() = runTest {
        viewModel.checkForAppUpdate()

        // Although this test does not copy the real world situation, as the initial and result objects
        // should be mostly the same, we test VM proper functionality. VM emits the initial object
        // defined in this class, then we expect the result object from the AppUpdateCheckerMock class
        // and a newly acquired AppUpdateInfo object.
        viewModel.updateInfo.take(2).collectIndexed { index, incomingInfo ->
            if (index == 0) {
                assertEquals(initialUpdateInfo.appUpdateInfo, incomingInfo.appUpdateInfo)
                assertEquals(initialUpdateInfo.priority, incomingInfo.priority)
                assertEquals(initialUpdateInfo.state, incomingInfo.state)
                assertEquals(initialUpdateInfo.isForce, incomingInfo.isForce)
            } else {
                assertNotNull(incomingInfo.appUpdateInfo)
                assertEquals(AppUpdateCheckerMock.resultUpdateInfo.priority, incomingInfo.priority)
                assertEquals(AppUpdateCheckerMock.resultUpdateInfo.state, incomingInfo.state)
                assertEquals(AppUpdateCheckerMock.resultUpdateInfo.isForce, incomingInfo.isForce)
            }
        }
    }

    @Test
    @MediumTest
    fun validate_result_of_go_for_update() = runTest {
        viewModel.goForUpdate(composeTestRule.activity, initialUpdateInfo.appUpdateInfo)

        // In this case we only test that the VM changes state once it finishes the update.
        viewModel.updateInfo.take(2).collectIndexed { index, incomingInfo ->
            if (index == 0) {
                assertEquals(UpdateState.Running, incomingInfo.state)
                assertEquals(initialUpdateInfo.appUpdateInfo, incomingInfo.appUpdateInfo)
                assertEquals(initialUpdateInfo.priority, incomingInfo.priority)
                assertEquals(initialUpdateInfo.isForce, incomingInfo.isForce)
            } else {
                assertEquals(UpdateState.Done, incomingInfo.state)
            }
        }
    }
}
