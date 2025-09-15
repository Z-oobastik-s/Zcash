package com.zoobastiks.zcash.managers

import com.zoobastiks.zcash.ZcashPlugin
import com.zoobastiks.zcash.utils.CurrencyItem
import com.zoobastiks.zcash.utils.getCurrencyAmount
import com.zoobastiks.zcash.utils.getCurrencySource
import com.zoobastiks.zcash.utils.setCurrencyData
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.util.concurrent.ConcurrentHashMap

class CurrencyOptimizationManager(private val plugin: ZcashPlugin) {
    
    private val currencyItems = ConcurrentHashMap<Item, Long>() // Item to spawn time
    private var optimizationTask: BukkitRunnable? = null
    
    fun startOptimization() {
        stopOptimization()
        
        optimizationTask = object : BukkitRunnable() {
            override fun run() {
                try {
                    processCurrencyOptimization()
                } catch (e: Exception) {
                    plugin.logger.warning("Error in currency optimization: ${e.message}")
                }
            }
        }
        
        // Run every 2 seconds (40 ticks)
        optimizationTask?.runTaskTimer(plugin, 40L, 40L)
    }
    
    fun stopOptimization() {
        optimizationTask?.cancel()
        optimizationTask = null
    }
    
    fun registerCurrencyItem(item: Item) {
        currencyItems[item] = System.currentTimeMillis()
    }
    
    fun unregisterCurrencyItem(item: Item) {
        currencyItems.remove(item)
        plugin.hologramManager.removeHologram(item)
    }
    
    private fun processCurrencyOptimization() {
        val currentTime = System.currentTimeMillis()
        val despawnTimeMs = plugin.configManager.getDespawnTime() * 1000L
        val stackRadius = plugin.configManager.getStackRadius()
        val hideDistance = plugin.configManager.getHideDistance()
        
        val toRemove = mutableListOf<Item>()
        val processedItems = mutableSetOf<Item>()
        
        for ((item, spawnTime) in currencyItems) {
            if (!item.isValid || item.isDead) {
                toRemove.add(item)
                continue
            }
            
            // Check despawn time
            if (currentTime - spawnTime > despawnTimeMs) {
                plugin.hologramManager.removeHologram(item)
                item.remove()
                toRemove.add(item)
                continue
            }
            
            // Skip if already processed in this tick
            if (processedItems.contains(item)) continue
            
            // Hide/show currency based on player distance
            processVisibility(item, hideDistance)
            
            // Stack currency if enabled
            if (plugin.configManager.isStackCurrencyEnabled()) {
                stackNearbyItems(item, stackRadius, processedItems)
            }
        }
        
        // Remove invalid items
        toRemove.forEach { currencyItems.remove(it) }
    }
    
    private fun processVisibility(item: Item, hideDistance: Double) {
        val location = item.location
        var hasNearbyPlayer = false
        
        for (player in Bukkit.getOnlinePlayers()) {
            if (player.world == location.world && 
                player.location.distance(location) <= hideDistance) {
                hasNearbyPlayer = true
                break
            }
        }
        
        // Show/hide item using custom name visibility
        // This is a simple approach - in a real implementation you might use packets
        if (hasNearbyPlayer && !item.isCustomNameVisible) {
            item.isCustomNameVisible = true
        } else if (!hasNearbyPlayer && item.isCustomNameVisible) {
            item.isCustomNameVisible = false
        }
    }
    
    private fun stackNearbyItems(centralItem: Item, radius: Double, processedItems: MutableSet<Item>) {
        if (processedItems.contains(centralItem)) return
        
        val centralLocation = centralItem.location
        val centralAmount = centralItem.itemStack.getCurrencyAmount()
        var totalAmount = centralAmount
        val itemsToStack = mutableListOf<Item>()
        
        // Find nearby currency items
        for ((otherItem, _) in currencyItems) {
            if (otherItem == centralItem || processedItems.contains(otherItem)) continue
            if (!otherItem.isValid || otherItem.isDead) continue
            
            val otherLocation = otherItem.location
            if (centralLocation.world == otherLocation.world && 
                centralLocation.distance(otherLocation) <= radius) {
                
                val otherAmount = otherItem.itemStack.getCurrencyAmount()
                totalAmount += otherAmount
                itemsToStack.add(otherItem)
            }
        }
        
        // If we found items to stack
        if (itemsToStack.isNotEmpty()) {
            // Update central item with new amount using extension function
            val source = centralItem.itemStack.getCurrencySource()
            centralItem.itemStack.setCurrencyData(totalAmount, source)
            
            // Update hologram
            plugin.hologramManager.removeHologram(centralItem)
            plugin.hologramManager.createHologram(centralItem, totalAmount)
            
            // Remove stacked items
            for (stackedItem in itemsToStack) {
                plugin.hologramManager.removeHologram(stackedItem)
                stackedItem.remove()
                currencyItems.remove(stackedItem)
                processedItems.add(stackedItem)
            }
        }
        
        processedItems.add(centralItem)
    }
    
    fun getCurrencyItemCount(): Int = currencyItems.size
}
