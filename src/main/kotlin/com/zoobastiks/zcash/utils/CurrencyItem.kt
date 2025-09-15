package com.zoobastiks.zcash.utils

import com.zoobastiks.zcash.ZcashPlugin
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack

/**
 * Modern currency item utilities using Component API and extension functions
 */
object CurrencyItem {
    
    /**
     * Creates a clean currency ItemStack with no visible name
     */
    fun create(plugin: ZcashPlugin, amount: Int, source: String = "UNKNOWN"): ItemStack {
        val material = plugin.configManager.getDisplayItem()
        
        return ItemStack(material, 1).apply {
            itemMeta = itemMeta?.apply {
                // Completely hide item name using modern Component API
                displayName(Component.empty())
                // Remove any lore to keep it clean
                lore(emptyList())
            }
            // Set currency data using extension function
            setCurrencyData(amount, source)
        }
    }
    
    /**
     * Drops currency item at location without side effects
     */
    fun drop(plugin: ZcashPlugin, location: Location, amount: Int, source: String = "UNKNOWN"): Item {
        val currencyItem = create(plugin, amount, source)
        return location.world.dropItemNaturally(location, currencyItem).apply {
            // Double protection: hide entity name too
            customName(Component.empty())
            isCustomNameVisible = false
        }
    }
    
    /**
     * Drops currency item with hologram and registers it with managers
     */
    fun dropWithHologram(plugin: ZcashPlugin, location: Location, amount: Int, source: String = "UNKNOWN"): Item {
        val droppedItem = drop(plugin, location, amount, source)
        
        // Register with managers
        plugin.hologramManager.createHologram(droppedItem, amount)
        plugin.currencyOptimizationManager.registerCurrencyItem(droppedItem)
        
        return droppedItem
    }
    
    // Legacy methods for compatibility (delegate to extension functions)
    @Deprecated("Use ItemStack.isCurrencyItem() extension", ReplaceWith("item.isCurrencyItem()"))
    fun isCurrencyItem(item: ItemStack): Boolean = item.isCurrencyItem()
    
    @Deprecated("Use ItemStack.getCurrencyAmount() extension", ReplaceWith("item.getCurrencyAmount()"))
    fun getAmountFromItem(item: ItemStack): Int = item.getCurrencyAmount()
    
    @Deprecated("Use ItemStack.getCurrencySource() extension", ReplaceWith("item.getCurrencySource()"))
    fun getSourceFromItem(item: ItemStack): String = item.getCurrencySource()
}
