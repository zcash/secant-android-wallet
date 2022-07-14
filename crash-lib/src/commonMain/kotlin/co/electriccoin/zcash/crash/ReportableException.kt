package co.electriccoin.zcash.crash

import kotlinx.datetime.Instant

data class ReportableException(
    val exception: Throwable,
    val appVersion: String,
    val isUncaught: Boolean,
    val time: Instant
) {

    companion object
}
