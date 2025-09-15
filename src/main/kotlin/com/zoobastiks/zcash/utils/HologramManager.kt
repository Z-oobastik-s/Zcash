package com.zoobastiks.zcash.utils

import com.zoobastiks.zcash.ZcashPlugin
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Item
import org.bukkit.scheduler.BukkitRunnable
import java.util.concurrent.ConcurrentHashMap

class HologramManager(private val plugin: ZcashPlugin) {
    
    private val holograms = ConcurrentHashMap<Item, ArmorStand>()
    
    fun createHologram(item: Item, amount: Int) {
        if (!plugin.configManager.isHologramEnabled()) return
        
        val location = item.location.clone().add(0.0, plugin.configManager.getHologramHeight(), 0.0)
        
        // Create modern Component-based hologram text
        val hologramComponent = Component.text("$amount ⛃")
            .color(NamedTextColor.GOLD)
            .decoration(TextDecoration.BOLD, true)
        
        val armorStand = location.world.spawn(location, ArmorStand::class.java) { stand ->
            stand.isVisible = false
            stand.isSmall = true
            stand.setGravity(false)
            stand.isCustomNameVisible = true
            stand.customName(hologramComponent) // Use Component API
            stand.isInvulnerable = true
            stand.setAI(false)
            stand.isSilent = true
            stand.setCanPickupItems(false)
            stand.isMarker = true // Makes it non-collidable
        }
        
        holograms[item] = armorStand
        
        // Start tracking task to update hologram position
        object : BukkitRunnable() {
            override fun run() {
                if (item.isDead || !item.isValid) {
                    removeHologram(item)
                    cancel()
                    return
                }
                
                // Update hologram position to follow the item
                val newLocation = item.location.clone().add(0.0, plugin.configManager.getHologramHeight(), 0.0)
                armorStand.teleport(newLocation)
            }
        }.runTaskTimer(plugin, 1L, 1L) // Update every tick
    }
    
    fun removeHologram(item: Item) {
        val armorStand = holograms.remove(item)
        armorStand?.remove()
    }
    
    fun removeAllHolograms() {
        holograms.values.forEach { it.remove() }
        holograms.clear()
    }
    
    fun getHologramCount(): Int = holograms.size
}
