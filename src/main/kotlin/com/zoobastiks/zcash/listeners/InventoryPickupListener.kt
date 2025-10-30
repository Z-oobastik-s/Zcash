package com.zoobastiks.zcash.listeners

import com.zoobastiks.zcash.ZcashPlugin
import com.zoobastiks.zcash.utils.isCurrencyItem
import org.bukkit.entity.minecart.HopperMinecart
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryPickupItemEvent

/**
 * Listener to prevent hoppers and hopper minecarts from picking up currency items
 * This ensures currency can only be picked up by players, preventing issues with
 * full player inventories and hopper-based farms
 */
class InventoryPickupListener(private val plugin: ZcashPlugin) : Listener {
    
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onInventoryPickupItem(event: InventoryPickupItemEvent) {
        val item = event.item.itemStack
        
        // Check if this is a currency item
        if (!item.isCurrencyItem()) return
        
        // Check what type of inventory is picking up the item
        val inventory = event.inventory
        val holder = inventory.holder
        
        when (holder) {
            // Hopper trying to pick up currency
            is org.bukkit.block.Hopper -> {
                // Check config setting for hoppers
                if (!plugin.configManager.allowHopperPickup()) {
                    event.isCancelled = true
                    
                    // Optional: Log for debugging
                    if (plugin.config.getBoolean("debug.log_hopper_attempts", false)) {
                        plugin.logger.info("Blocked hopper at ${holder.location} from picking up currency")
                    }
                }
            }
            
            // Hopper minecart trying to pick up currency
            is HopperMinecart -> {
                // Check config setting for hopper minecarts
                if (!plugin.configManager.allowHopperMinecartPickup()) {
                    event.isCancelled = true
                    
                    // Optional: Log for debugging
                    if (plugin.config.getBoolean("debug.log_hopper_attempts", false)) {
                        plugin.logger.info("Blocked hopper minecart from picking up currency")
                    }
                }
            }
            
            // Other inventory holders (if any exist in future Minecraft versions)
            else -> {
                // By default, block all non-player pickups for safety
                // Players use PlayerPickupItemEvent, not this event
                event.isCancelled = true
            }
        }
    }
}

