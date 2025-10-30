package com.zoobastiks.zcash.listeners

import com.zoobastiks.zcash.ZcashPlugin
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent

/**
 * Listener for tracking blocks placed by players
 * This prevents coin duplication exploit where players place and break the same block repeatedly
 * Performance: O(1) HashMap operation - extremely fast, minimal CPU usage
 */
class BlockPlaceListener(private val plugin: ZcashPlugin) : Listener {
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onBlockPlace(event: BlockPlaceEvent) {
        // Skip if anti-duplication is disabled
        if (!plugin.configManager.isAntiDuplicationEnabled()) return
        
        val block = event.block
        
        // Check if plugin is enabled in this world
        if (!plugin.configManager.isWorldEnabled(block.world.name)) {
            return
        }
        
        // Only track if event is not cancelled
        if (event.isCancelled) return
        
        // Mark this block as player-placed (O(1) operation)
        plugin.placedBlocksManager.addPlacedBlock(block.location)
    }
}

