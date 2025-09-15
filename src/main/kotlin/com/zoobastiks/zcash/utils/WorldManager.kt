package com.zoobastiks.zcash.utils

import com.zoobastiks.zcash.ZcashPlugin
import org.bukkit.entity.Player

/**
 * Utility class for managing world-specific plugin functionality
 */
object WorldManager {
    
    /**
     * Checks if the plugin is enabled in the given world and optionally sends a message to the player
     * @param plugin The plugin instance
     * @param player The player to check and notify
     * @param sendMessage Whether to send a disabled message if the world is not enabled
     * @return true if the plugin is enabled in the player's world, false otherwise
     */
    fun isEnabledInWorld(plugin: ZcashPlugin, player: Player, sendMessage: Boolean = true): Boolean {
        val worldName = player.world.name
        val isEnabled = plugin.configManager.isWorldEnabled(worldName)
        
        if (!isEnabled && sendMessage && plugin.configManager.shouldShowDisabledMessages()) {
            val message = plugin.messagesManager.getMessage("world-disabled")
            player.sendMessage(plugin.messageFormatter.format(message))
        }
        
        return isEnabled
    }
    
    /**
     * Checks if the plugin is enabled in the given world name
     * @param plugin The plugin instance
     * @param worldName The name of the world to check
     * @return true if the plugin is enabled in the world, false otherwise
     */
    fun isEnabledInWorld(plugin: ZcashPlugin, worldName: String): Boolean {
        return plugin.configManager.isWorldEnabled(worldName)
    }
}
