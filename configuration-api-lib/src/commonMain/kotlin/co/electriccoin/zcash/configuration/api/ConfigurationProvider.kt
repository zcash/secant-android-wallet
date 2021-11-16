package co.electriccoin.zcash.configuration.api

import co.electriccoin.zcash.configuration.model.map.Configuration
import kotlinx.coroutines.flow.Flow

/**
 * Provides a remote config implementation.
 */
interface ConfigurationProvider {

    /**
     * @return The configuration if it has been loaded already.  If not loaded, returns an empty configuration.
     */
    fun peekConfiguration()

    /**
     * @return Snapshots of configuration updates.
     */
    fun getConfigurationFlow(): Flow<Configuration>

    /**
     * Signals to the configuration provider that now might be a good time to refresh.
     */
    fun hintToRefresh()
}
