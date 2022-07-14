package co.electriccoin.zcash.crash.android.internal

import android.content.Context
import android.os.Bundle
import co.electriccoin.zcash.crash.ReportableException
import co.electriccoin.zcash.spackle.AndroidApiVersion
import co.electriccoin.zcash.spackle.getPackageInfoCompat
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

fun ReportableException.Companion.new(
    context: Context,
    throwable: Throwable,
    isUncaught: Boolean,
    clock: Clock = Clock.System
): ReportableException {
    val versionName = context.packageManager.getPackageInfoCompat(context.packageName, 0L).versionName
        ?: "null"

    return ReportableException(
        throwable,
        versionName,
        isUncaught,
        clock.now()
    )
}

fun ReportableException.toBundle() = Bundle().apply {
    putSerializable(ReportableException.EXTRA_SERIALIZABLE_THROWABLE, exception)
    putString(ReportableException.EXTRA_STRING_APP_VERSION, appVersion)
    putBoolean(ReportableException.EXTRA_BOOLEAN_IS_UNCAUGHT, isUncaught)
    putLong(ReportableException.EXTRA_LONG_WALLTIME_MILLIS, time.toEpochMilliseconds())
}

fun ReportableException.Companion.fromBundle(bundle: Bundle): ReportableException {
    val throwable: Throwable = if (co.electriccoin.zcash.spackle.AndroidApiVersion.isAtLeastT) {
        bundle.getSerializable(EXTRA_SERIALIZABLE_THROWABLE, kotlin.Throwable::class.java)!!
    } else {
        @Suppress("Deprecation")
        bundle.getSerializable(EXTRA_SERIALIZABLE_THROWABLE)!! as Throwable
    }
    val appVersion = bundle.getString(EXTRA_STRING_APP_VERSION)!!
    val isUncaught = bundle.getBoolean(EXTRA_BOOLEAN_IS_UNCAUGHT, false)
    val time = Instant.fromEpochMilliseconds(bundle.getLong(EXTRA_LONG_WALLTIME_MILLIS, 0))

    return ReportableException(throwable, appVersion, isUncaught, time)
}

private val ReportableException.Companion.EXTRA_SERIALIZABLE_THROWABLE
    get() = "co.electriccoin.zcash.crash.extra.SERIALIZABLE_THROWABLE" // $NON-NLS-1$

private val ReportableException.Companion.EXTRA_STRING_APP_VERSION: String
    get() = "co.electriccoin.zcash.crash.extra.STRING_APP_VERSION" // $NON-NLS-1$

private val ReportableException.Companion.EXTRA_BOOLEAN_IS_UNCAUGHT
    get() = "co.electriccoin.zcash.crash.extra.BOOLEAN_IS_UNCAUGHT" // $NON-NLS-1$

private val ReportableException.Companion.EXTRA_LONG_WALLTIME_MILLIS
    get() = "co.electriccoin.zcash.crash.extra.LONG_WALLTIME_MILLIS" // $NON-NLS-1$
