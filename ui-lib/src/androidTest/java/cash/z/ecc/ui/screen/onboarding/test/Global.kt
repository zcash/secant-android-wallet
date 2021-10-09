package cash.z.ecc.ui.screen.onboarding.test

import android.content.Context
import androidx.annotation.StringRes
import androidx.test.core.app.ApplicationProvider

fun getStringResource(@StringRes resId: Int) = ApplicationProvider.getApplicationContext<Context>().getString(resId)
