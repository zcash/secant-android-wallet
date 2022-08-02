package cash.z.ecc.sdk.model

import android.app.Application
import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.toEntropy
import cash.z.ecc.android.sdk.model.BlockHeight
import cash.z.ecc.android.sdk.model.ZcashNetwork
import cash.z.ecc.sdk.type.fromResources
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Represents everything needed to save and restore a wallet.
 */
data class PersistableWallet(
    val network: ZcashNetwork,
    val birthday: BlockHeight?,
    val seedPhrase: SeedPhrase
) {

    /**
     * @return Wallet serialized to JSON format, suitable for long-term encrypted storage.
     */
    // Note: We're using a hand-crafted serializer so that we're less likely to have accidental
    // breakage from reflection or annotation based methods, and so that we can carefully manage versioning.
    fun toJson() = JSONObject().apply {
        put(KEY_VERSION, VERSION_1)
        put(KEY_NETWORK_ID, network.id)
        birthday?.let {
            put(KEY_BIRTHDAY, it.value)
        }
        put(KEY_SEED_PHRASE, seedPhrase.joinToString())
    }

    // For security, intentionally override the toString method to reduce risk of accidentally logging secrets
    override fun toString() = "PersistableWallet"

    companion object {
        private const val VERSION_1 = 1

        internal const val KEY_VERSION = "v"
        internal const val KEY_NETWORK_ID = "network_ID"
        internal const val KEY_BIRTHDAY = "birthday"
        internal const val KEY_SEED_PHRASE = "seed_phrase"

        fun from(jsonObject: JSONObject): PersistableWallet {
            when (val version = jsonObject.getInt(KEY_VERSION)) {
                VERSION_1 -> {
                    val network = run {
                        val networkId = jsonObject.getInt(KEY_NETWORK_ID)
                        ZcashNetwork.from(networkId)
                    }
                    val birthday = if (jsonObject.has(KEY_BIRTHDAY)) {
                        val birthdayBlockHeightLong = jsonObject.getLong(KEY_BIRTHDAY)
                        BlockHeight.new(network, birthdayBlockHeightLong)
                    } else {
                        null
                    }
                    val seedPhrase = jsonObject.getString(KEY_SEED_PHRASE)

                    return PersistableWallet(network, birthday, SeedPhrase.new(seedPhrase))
                }
                else -> {
                    throw IllegalArgumentException("Unsupported version $version")
                }
            }
        }

        /**
         * @return A new PersistableWallet with a random seed phrase.
         */
        suspend fun new(application: Application): PersistableWallet {
            val zcashNetwork = ZcashNetwork.fromResources(application)
            val birthday = BlockHeight.ofLatestCheckpoint(application, zcashNetwork)

            val seedPhrase = newSeedPhrase()

            return PersistableWallet(zcashNetwork, birthday, seedPhrase)
        }
    }
}

// Using IO context because of https://github.com/zcash/kotlin-bip39/issues/13
private suspend fun newMnemonic() = withContext(Dispatchers.IO) {
    Mnemonics.MnemonicCode(cash.z.ecc.android.bip39.Mnemonics.WordCount.COUNT_24.toEntropy()).words
}

private suspend fun newSeedPhrase() = SeedPhrase(newMnemonic().map { it.concatToString() })
