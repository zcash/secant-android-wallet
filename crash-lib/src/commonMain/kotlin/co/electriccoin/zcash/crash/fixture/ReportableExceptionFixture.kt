package co.electriccoin.zcash.crash.fixture

import co.electriccoin.zcash.crash.ReportableException
import kotlinx.datetime.Instant
import kotlinx.datetime.toInstant

object ReportableExceptionFixture {
    val EXCEPTION = RuntimeException("I am exceptional")
    const val APP_VERSION = "1.0.2"
    const val IS_UNCAUGHT = true

    // No milliseconds, because those can cause some tests to fail due to rounding
    val TIMESTAMP = "2022-04-15T11:28:54Z".toInstant()

    fun new(
        exception: Throwable = EXCEPTION,
        appVersion: String = APP_VERSION,
        isUncaught: Boolean = IS_UNCAUGHT,
        time: Instant = TIMESTAMP
    ) = ReportableException(exception, appVersion, isUncaught, time)
}
