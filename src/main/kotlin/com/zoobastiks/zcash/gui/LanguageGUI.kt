package com.zoobastiks.zcash.gui

import com.zoobastiks.zcash.ZcashPlugin
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class LanguageGUI(private val plugin: ZcashPlugin) : Listener {
    
    fun openLanguageMenu(player: Player) {
        val title = plugin.messageFormatter.format(plugin.messagesManager.getMessage("language-gui-title"))
        val inventory = Bukkit.createInventory(null, 27, title)
        
        // Russian flag (Red)
        val russianItem = createLanguageItem(
            Material.RED_BANNER,
            plugin.messagesManager.getMessage("language-gui-russian"),
            plugin.messagesManager.getMessage("language-gui-russian-lore"),
            "ru"
        )
        
        // English flag (Blue)
        val englishItem = createLanguageItem(
            Material.BLUE_BANNER,
            plugin.messagesManager.getMessage("language-gui-english"),
            plugin.messagesManager.getMessage("language-gui-english-lore"),
            "en"
        )
        
        // Chinese flag (Yellow)
        val chineseItem = createLanguageItem(
            Material.YELLOW_BANNER,
            plugin.messagesManager.getMessage("language-gui-chinese"),
            plugin.messagesManager.getMessage("language-gui-chinese-lore"),
            "zh"
        )
        
        // Place items in GUI
        inventory.setItem(11, russianItem) // Left
        inventory.setItem(13, englishItem) // Middle  
        inventory.setItem(15, chineseItem) // Right
        
        // Add decorative glass panes
        val glassPane = ItemStack(Material.GRAY_STAINED_GLASS_PANE).apply {
            itemMeta = itemMeta?.apply {
                displayName(Component.text(" "))
            }
        }
        
        // Fill empty slots with glass panes
        for (i in 0 until inventory.size) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, glassPane)
            }
        }
        
        player.openInventory(inventory)
    }
    
    private fun createLanguageItem(material: Material, name: String, lore: String, languageCode: String): ItemStack {
        return ItemStack(material).apply {
            itemMeta = itemMeta?.apply {
                // Set display name using Component API
                val formattedName = plugin.messageFormatter.format(name)
                displayName(Component.text(formattedName).decoration(TextDecoration.ITALIC, false))
                
                // Set lore
                val formattedLore = plugin.messageFormatter.format(lore)
                lore(listOf(
                    Component.text(formattedLore).decoration(TextDecoration.ITALIC, false),
                    Component.empty(),
                    Component.text("Language: $languageCode").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
                ))
                
                // Add custom model data to identify language
                setCustomModelData(when (languageCode) {
                    "ru" -> 1
                    "en" -> 2
                    "zh" -> 3
                    else -> 0
                })
            }
        }
    }
    
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val title = plugin.messageFormatter.format(plugin.messagesManager.getMessage("language-gui-title"))
        
        if (event.view.title != title) return
        
        event.isCancelled = true
        
        val clickedItem = event.currentItem ?: return
        val meta = clickedItem.itemMeta ?: return
        
        val languageCode = when (meta.customModelData) {
            1 -> "ru"
            2 -> "en" 
            3 -> "zh"
            else -> return
        }
        
        // Change language
        plugin.messagesManager.changeLanguage(languageCode)
        
        // Send confirmation message
        val confirmMessage = plugin.messagesManager.getMessage("language-changed")
        player.sendMessage(plugin.messageFormatter.format(confirmMessage))
        
        // Close inventory
        player.closeInventory()
        
        // Log language change
        plugin.logger.info("Player ${player.name} changed language to $languageCode")
    }
}
