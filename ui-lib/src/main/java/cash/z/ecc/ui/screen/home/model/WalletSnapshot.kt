package cash.z.ecc.ui.screen.home.model

import cash.z.ecc.android.sdk.Synchronizer
import cash.z.ecc.android.sdk.block.CompactBlockProcessor
import cash.z.ecc.android.sdk.ext.ZcashSdk
import cash.z.ecc.android.sdk.type.WalletBalance
import cash.z.ecc.sdk.model.Zatoshi

data class WalletSnapshot(
    val status: Synchronizer.Status,
    val processorInfo: CompactBlockProcessor.ProcessorInfo,
    val orchardBalance: WalletBalance,
    val saplingBalance: WalletBalance,
    val transparentBalance: WalletBalance,
    val unminedCount: Int
) {
    // Note: the wallet is effectively empty if it cannot cover the miner's fee
    val hasFunds = saplingBalance.availableZatoshi > (ZcashSdk.MINERS_FEE_ZATOSHI.toDouble() / ZcashSdk.ZATOSHI_PER_ZEC) // 0.00001
    val hasSaplingBalance = saplingBalance.totalZatoshi > 0

    val isSendEnabled: Boolean get() = status == Synchronizer.Status.SYNCED && hasFunds
}

fun WalletSnapshot.totalBalance(): Zatoshi {
    val total = (orchardBalance + saplingBalance + transparentBalance).totalZatoshi

    return Zatoshi(total.coerceAtLeast(0))
}
