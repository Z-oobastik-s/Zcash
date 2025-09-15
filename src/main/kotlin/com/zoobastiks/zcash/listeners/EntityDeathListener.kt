package com.zoobastiks.zcash.listeners

import com.zoobastiks.zcash.ZcashPlugin
import com.zoobastiks.zcash.utils.CurrencyItem
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import kotlin.random.Random

class EntityDeathListener(private val plugin: ZcashPlugin) : Listener {
    
    @EventHandler(priority = EventPriority.NORMAL)
    fun onEntityDeath(event: EntityDeathEvent) {
        val killer = event.entity.killer
        if (killer !is Player) return
        
        // Check if plugin is enabled in this world
        val location = event.entity.location
        if (!plugin.configManager.isWorldEnabled(location.world.name)) {
            return
        }
        
        val entityType = event.entity.type.name.lowercase()
        
        // Get drop settings for this mob
        val amount = plugin.configManager.getMobAmount(entityType)
        val chance = plugin.configManager.getMobChance(entityType)
        
        // Check if drop should occur based on chance
        if (Random.nextInt(1, 101) > chance) return
        
        // Parse amount (could be range like "1-3" or single number like "5")
        val dropAmount = plugin.configManager.parseAmount(amount)
        
        if (dropAmount <= 0) return
        
        // Create currency item and drop it with hologram
        CurrencyItem.dropWithHologram(plugin, location, dropAmount, "MOB")
        
        // Optional: Send debug message to player
        if (plugin.config.getBoolean("debug.show_drops", false)) {
            val message = plugin.messagesManager.getMessage("currency-dropped", mapOf(
                "amount" to dropAmount.toString()
            ))
            killer.sendMessage(plugin.messageFormatter.format(message))
        }
    }
}
