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
        
        // Limit processing to prevent lag - configurable from config.yml
        val maxItemsPerTick = plugin.configManager.getMaxItemsPerTick()
        var processedCount = 0
        
        val toRemove = mutableListOf<Item>()
        val processedItems = mutableSetOf<Item>()
        
        // Process items in iterator to avoid concurrent modification
        val iterator = currencyItems.iterator()
        while (iterator.hasNext() && processedCount < maxItemsPerTick) {
            val (item, spawnTime) = iterator.next()
            processedCount++
            
            if (!item.isValid || item.isDead) {
                iterator.remove()
                plugin.hologramManager.removeHologram(item)
                continue
            }
            
            // Check despawn time
            if (currentTime - spawnTime > despawnTimeMs) {
                plugin.hologramManager.removeHologram(item)
                item.remove()
                iterator.remove()
                continue
            }
            
            // Skip if already processed in this tick
            if (item in processedItems) continue
            
            // Hide/show currency based on player distance
            processVisibility(item, hideDistance)
            
            // Stack currency if enabled (limit stacking operations per tick)
            val maxStackOperations = plugin.configManager.getMaxStackChecks() * 2
            if (plugin.configManager.isStackCurrencyEnabled() && processedItems.size < maxStackOperations) {
                stackNearbyItems(item, stackRadius, processedItems)
            }
        }
        
        // Log performance warning if too many items
        val warningThreshold = plugin.configManager.getWarningThreshold()
        if (warningThreshold > 0 && currencyItems.size > warningThreshold) {
            plugin.logger.warning("High currency item count (${currencyItems.size}). Consider reducing despawn time for better performance.")
        }
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
        if (centralItem in processedItems) return
        
        val centralLocation = centralItem.location
        val centralAmount = centralItem.itemStack.getCurrencyAmount()
        var totalAmount = centralAmount
        val itemsToStack = mutableListOf<Item>()
        
        // Limit nearby search to prevent lag - configurable from config.yml
        var checkedCount = 0
        val maxChecksPerStack = plugin.configManager.getMaxStackChecks()
        
        // Find nearby currency items with optimized iteration
        for ((otherItem, _) in currencyItems) {
            if (checkedCount >= maxChecksPerStack) break
            checkedCount++
            
            if (otherItem == centralItem || otherItem in processedItems) continue
            if (!otherItem.isValid || otherItem.isDead) continue
            
            val otherLocation = otherItem.location
            // Quick world check first (cheaper than distance calculation)
            if (centralLocation.world != otherLocation.world) continue
            
            // Use distanceSquared for performance (avoids sqrt calculation)
            val radiusSquared = radius * radius
            if (centralLocation.distanceSquared(otherLocation) <= radiusSquared) {
                val otherAmount = otherItem.itemStack.getCurrencyAmount()
                totalAmount += otherAmount
                itemsToStack.add(otherItem)
                
                // Limit maximum items to stack to prevent massive operations
                if (itemsToStack.size >= plugin.configManager.getMaxStackSize()) break
            }
        }
        
        // If we found items to stack
        if (itemsToStack.isNotEmpty()) {
            try {
                // Update central item with new amount using extension function
                val source = centralItem.itemStack.getCurrencySource()
                centralItem.itemStack.setCurrencyData(totalAmount, source)
                
                // Update hologram
                plugin.hologramManager.removeHologram(centralItem)
                plugin.hologramManager.createHologram(centralItem, totalAmount)
                
                // Remove stacked items efficiently
                for (stackedItem in itemsToStack) {
                    plugin.hologramManager.removeHologram(stackedItem)
                    stackedItem.remove()
                    currencyItems.remove(stackedItem)
                    processedItems.add(stackedItem)
                }
                
                // Log debug info for large stacks (only if debug enabled)
                if (itemsToStack.size > 3 && plugin.config.getBoolean("debug.log_stacking", false)) {
                    plugin.logger.info("Stacked ${itemsToStack.size} currency items into one (total: $totalAmount)")
                }
            } catch (e: Exception) {
                plugin.logger.warning("Error during currency stacking: ${e.message}")
            }
        }
        
        processedItems.add(centralItem)
    }
    
    fun getCurrencyItemCount(): Int = currencyItems.size
}
