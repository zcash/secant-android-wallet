package cash.z.ecc.sdk.fixture

import cash.z.ecc.android.sdk.model.BlockHeight
import cash.z.ecc.android.sdk.model.ZcashNetwork
import cash.z.ecc.sdk.model.PersistableWallet
import cash.z.ecc.sdk.model.SeedPhrase

object PersistableWalletFixture {

    val NETWORK = ZcashNetwork.Testnet

    // These came from the mainnet 1500000.json file
    @Suppress("MagicNumber")
    val BIRTHDAY = BlockHeight.new(ZcashNetwork.Mainnet, 1500000L)

    val SEED_PHRASE = SeedPhraseFixture.new()

    fun new(
        network: ZcashNetwork = NETWORK,
        birthday: BlockHeight = BIRTHDAY,
        seedPhrase: SeedPhrase = SEED_PHRASE
    ) = PersistableWallet(network, birthday, seedPhrase)
}
