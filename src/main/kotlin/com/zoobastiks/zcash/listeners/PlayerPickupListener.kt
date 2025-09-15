package com.zoobastiks.zcash.listeners

import com.zoobastiks.zcash.ZcashPlugin
import com.zoobastiks.zcash.database.DatabaseManager
import com.zoobastiks.zcash.utils.CurrencyItem
import com.zoobastiks.zcash.utils.getCurrencyAmount
import com.zoobastiks.zcash.utils.getCurrencySource
import com.zoobastiks.zcash.utils.isCurrencyItem
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerPickupItemEvent

@Suppress("DEPRECATION")
class PlayerPickupListener(private val plugin: ZcashPlugin) : Listener {
    
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerPickupItem(event: PlayerPickupItemEvent) {
        val item = event.item.itemStack
        val player = event.player
        
        // Check if plugin is enabled in this world
        if (!plugin.configManager.isWorldEnabled(player.world.name)) {
            return
        }
        
        // Check if this is a currency item using extension function
        if (!item.isCurrencyItem()) return
        
        // Get the amount from the item using extension function
        val amount = item.getCurrencyAmount()
        if (amount <= 0) return
        
        // Unregister from optimization manager
        plugin.currencyOptimizationManager.unregisterCurrencyItem(event.item)
        
        // Cancel the pickup event to prevent the item from going to inventory
        event.isCancelled = true
        
        // Remove the item from the world
        event.item.remove()
        
        // Give money to player through economy system
        val success = plugin.economyManager.giveMoney(player, amount.toDouble())
        
        if (success) {
            // Execute additional pickup commands if enabled
            if (plugin.configManager.isCommandsEnabled()) {
                plugin.economyManager.executePickupCommands(player, amount)
            }
            
            // Play pickup sound
            if (plugin.configManager.isPickupSoundEnabled()) {
                try {
                    val soundName = plugin.configManager.getPickupSound()
                    val sound = Sound.valueOf(soundName.uppercase().replace("MINECRAFT:", ""))
                    player.playSound(
                        player.location,
                        sound,
                        plugin.configManager.getPickupVolume(),
                        plugin.configManager.getPickupPitch()
                    )
                } catch (e: IllegalArgumentException) {
                    // Fallback to default sound if custom sound is invalid
                    player.playSound(player.location, Sound.ENTITY_AXOLOTL_SWIM, 1.0f, 1.0f)
                    plugin.logger.warning("Invalid pickup sound: ${plugin.configManager.getPickupSound()}")
                }
            }
            
            // Update player statistics in database using extension function
            val source = item.getCurrencySource()
            val earningSource = when (source) {
                "MOB" -> DatabaseManager.EarningSource.MOB
                "BLOCK" -> DatabaseManager.EarningSource.BLOCK
                else -> DatabaseManager.EarningSource.MOB // Default fallback
            }
            plugin.databaseManager.addEarnings(player, amount, earningSource)
            
            // Send pickup notification to player
            plugin.notificationManager.sendPickupNotification(player, amount)
        } else {
            // If economy transaction failed, send error message
            val errorMessage = plugin.messagesManager.getMessage("economy-transaction-failed")
            player.sendMessage(plugin.messageFormatter.format(errorMessage))
            
            // Optionally, re-drop the item if transaction failed using extension function
            val location = event.item.location
            val source = item.getCurrencySource()
            CurrencyItem.dropWithHologram(plugin, location, amount, source)
        }
    }
}
