package co.electriccoin.zcash.preference.model.entry

import kotlin.jvm.JvmInline

/**
 * Defines a preference key.
 *
 * Different preference providers may have unique restrictions on keys.  This attempts to
 * find a least common denominator with some reasonable limits on what the keys can contain.
 */
@JvmInline
value class PreferenceKey(val key: String) {
    init {
        requireKeyConstraints(key)
    }

    companion object {
        private const val MIN_KEY_LENGTH = 1
        private const val MAX_KEY_LENGTH = 256

        private val REGEX = Regex("[a-zA-Z0-9_]*") // $NON-NLS

        /**
         * Checks a preference key against known constraints.
         *
         * @param key Key to check.
         */
        private fun requireKeyConstraints(key: String) {
            require(key.length in 1..MAX_KEY_LENGTH) {
                "Invalid key $key. Length (${key.length}) should be [$MIN_KEY_LENGTH, $MAX_KEY_LENGTH]." // $NON-NLS
            }

            require(REGEX.matches(key)) { "Invalid key $key.  Key must contain only letter and numbers." } // $NON-NLS
        }
    }
}
