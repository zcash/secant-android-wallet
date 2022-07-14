@file:Suppress("ktlint:filename")

package co.electriccoin.zcash.crash

import co.electriccoin.zcash.spackle.io.writeAtomically
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

suspend fun ReportableException.write(path: File) {
    val exceptionString = buildString {
        appendLine("App version: $appVersion")
        appendLine("Is uncaught: $isUncaught")
        appendLine("Time: $time")
        append(exception.stackTraceToString())
    }

    withContext(Dispatchers.IO) {
        path.writeAtomically { tempFile ->
            tempFile.writeText(exceptionString)
        }
    }
}
