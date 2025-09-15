package com.zoobastiks.zcash.listeners

import com.zoobastiks.zcash.ZcashPlugin
import com.zoobastiks.zcash.utils.CurrencyItem
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import kotlin.random.Random

class BlockBreakListener(private val plugin: ZcashPlugin) : Listener {
    
    @EventHandler(priority = EventPriority.NORMAL)
    fun onBlockBreak(event: BlockBreakEvent) {
        val player = event.player
        val block = event.block
        
        // Check if plugin is enabled in this world
        if (!plugin.configManager.isWorldEnabled(block.world.name)) {
            return
        }
        
        // Don't drop currency in creative mode
        if (player.gameMode == GameMode.CREATIVE) return
        
        // Don't drop if event is cancelled
        if (event.isCancelled) return
        
        val blockType = event.block.type.name.lowercase()
        
        // Get drop settings for this block
        val amount = plugin.configManager.getBlockAmount(blockType)
        val chance = plugin.configManager.getBlockChance(blockType)
        
        // Check if drop should occur based on chance
        if (Random.nextInt(1, 101) > chance) return
        
        // Parse amount (could be range like "1-3" or single number like "5")
        val dropAmount = plugin.configManager.parseAmount(amount)
        
        if (dropAmount <= 0) return
        
        // Create currency item and drop it with hologram
        val location = event.block.location.add(0.5, 0.5, 0.5) // Center of block
        CurrencyItem.dropWithHologram(plugin, location, dropAmount, "BLOCK")
        
        // Optional: Send debug message to player
        if (plugin.config.getBoolean("debug.show_drops", false)) {
            val message = plugin.messagesManager.getMessage("currency-dropped", mapOf(
                "amount" to dropAmount.toString()
            ))
            player.sendMessage(plugin.messageFormatter.format(message))
        }
    }
}
