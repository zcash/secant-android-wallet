package co.electriccoin.zcash.configuration.model.entry

import kotlin.jvm.JvmInline

/**
 * Defines a configuration key.
 *
 * Different configuration providers have unique restrictions on keys.  This attempts to find a
 * least common denominator with some reasonable limits on what the keys can contain.
 */
@JvmInline
value class ConfigKey(val key: String) {
    init {
        requireKeyConstraints(key)
    }

    companion object {
        private const val MIN_KEY_LENGTH = 1
        private const val MAX_KEY_LENGTH = 256

        private val REGEX = Regex("[a-zA-Z0-9_]*")

        /**
         * Checks a configuration key against known constraints.
         *
         * @param key Key to check.
         */
        private fun requireKeyConstraints(key: String) {
            require(key.length in 1..MAX_KEY_LENGTH) { "Invalid key $key.  Length (${key.length}) is not in the range [$MIN_KEY_LENGTH, $MAX_KEY_LENGTH]." }

            // This is a Firebase requirement
            require(!key.first().isDigit()) { "Invalid key $key. Key must not start with a number." }

            require(REGEX.matches(key)) { "Invalid key $key.  Key must contain only letter and numbers." }
        }
    }
}
