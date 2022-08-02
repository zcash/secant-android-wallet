@file:Suppress("ktlint:filename")

package cash.z.ecc.sdk.type

import android.content.Context
import cash.z.ecc.android.sdk.model.ZcashNetwork
import cash.z.ecc.sdk.ext.R

/*
 * Note: If we end up having trouble with this implementation in the future, especially with the rollout
 * of disabling transitive resources, we do have alternative implementations.
 *
 * Probably the most straightforward and high performance would be to implement an interface, have
 * the Application class implement the interface, and allow this to cast the Application object to
 * get the value.  If the Application does not implement the interface, then the Mainnet can be the
 * default.
 *
 * Alternatives include
 *  - Adding build variants to sdk-ext-lib, ui-lib, and app which gets complex.  The current approach
 *    or the approach outlined above only requires build variants on the app module.
 *  - Using a ContentProvider for dynamic injection, where the URI is defined
 *  - Using AndroidManifest metadata for dynamic injection
 */
/**
 * @return Zcash network determined from resources.  A resource overlay of [R.bool.zcash_is_testnet]
 * can be used for different build variants to change the network type.
 */
fun ZcashNetwork.Companion.fromResources(context: Context) =
    if (context.resources.getBoolean(R.bool.zcash_is_testnet)) {
        ZcashNetwork.Testnet
    } else {
        ZcashNetwork.Mainnet
    }
