package cash.z.ecc.sdk.ext.ui

import android.content.Context
import cash.z.ecc.sdk.ext.ui.model.MonetarySeparators

object ZecStringExt {

    private const val DIGITS_BETWEEN_GROUP_SEPARATORS = 3

    /**
     * Builds filter with current local monetary separators for continuous input checking. The
     * solution is built upon regex validation and other common string validation checks.
     *
     * Regex example: ^([0-9]*([0-9]+([,]$|[,][0-9]+))*([.]$|[.][0-9]+)?)?$
     * Inputs may differ according to user locale.
     *
     * Valid amounts: "" . | .123 | 123, | 123. | 123,456 | 123.456 | 123,456.789 | 123,456,789 | 123,456,789.123 | etc.
     * Invalid amounts: 123,, | 123,. | 123.. | ,123 | 123.456.789 | etc.
     *
     * @param context used for loading localized pattern from strings.xml
     * @param separators which consist of localized monetary separators
     * @param zecString to be validated
     *
     * @return true in case of validation success, false otherwise
     */
    fun filterContinuous(context: Context, separators: MonetarySeparators, zecString: String): Boolean {
        if (!context.getString(
                R.string.zec_amount_regex_continuous_filter,
                separators.grouping,
                separators.decimal
            ).toRegex().matches(zecString) || !checkFor3Digits(separators, zecString)
        ) {
            return false
        }
        return true
    }

    /**
     * Checks for at least 3 digits between grouping separators.
     *
     * @param separators which consist of localized monetary separators
     * @param zecString to be validated
     *
     * @return true in case of validation success, false otherwise
     */
    fun checkFor3Digits(separators: MonetarySeparators, zecString: String): Boolean {
        if (zecString.count { it == separators.grouping } >= 2) {
            val groups = zecString.split(separators.grouping)
            for (i in 1 until (groups.size - 1)) {
                if (groups[i].length != DIGITS_BETWEEN_GROUP_SEPARATORS) {
                    return false
                }
            }
        }
        return true
    }

    /**
     * Builds filter with current local monetary separators for validation of entered ZEC amount
     * after confirm button is pressed. The solution is built upon regex validation and other common
     * string validation checks.
     *
     * Regex example: ^([0-9]{1,3}(?:[,]?[0-9]{3})*)*(?:[0-9]*[.][0-9]*)?$
     * Inputs may differ according to user locale.
     *
     * Valid amounts: 123 | .123 | 123. | 123, | 123.456 | 123,456 | 123,456.789 | 123,456,789 | 123,456,789.123 | etc.
     * Invalid amounts: "" | , | . | 123,, | 123,. | 123.. | ,123 | 123.456.789 | etc.
     *
     * @param context used for loading localized pattern from strings.xml
     * @param separators which consist of localized monetary separators
     * @param zecString to be validated
     *
     * @return true in case of validation success, false otherwise
     */
    fun filterConfirm(context: Context, separators: MonetarySeparators, zecString: String): Boolean {
        if (zecString.isBlank() ||
            zecString == separators.grouping.toString() ||
            zecString == separators.decimal.toString()
        ) {
            return false
        }

        return (
            context.getString(
                R.string.zec_amount_regex_confirm_filter,
                separators.grouping,
                separators.decimal
            ).toRegex().matches(zecString) && checkFor3Digits(separators, zecString)
            )
    }
}
