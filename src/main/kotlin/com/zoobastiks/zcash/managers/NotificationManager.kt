package com.zoobastiks.zcash.managers

import com.zoobastiks.zcash.ZcashPlugin
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

class NotificationManager(private val plugin: ZcashPlugin) {
    
    private val activeBossBars = ConcurrentHashMap<Player, BossBar>()
    
    fun sendPickupNotification(player: Player, amount: Int) {
        val notificationType = plugin.configManager.getPickupNotificationType().uppercase()
        
        when (notificationType) {
            "CHAT" -> sendChatNotification(player, amount)
            "ACTIONBAR" -> sendActionBarNotification(player, amount)
            "BOSSBAR" -> sendBossBarNotification(player, amount)
            "TITLE" -> sendTitleNotification(player, amount)
            "NONE" -> { /* No notification */ }
            else -> sendActionBarNotification(player, amount) // Default fallback
        }
    }
    
    private fun sendChatNotification(player: Player, amount: Int) {
        val message = plugin.messagesManager.getMessage("pickup", mapOf("amount" to amount.toString()))
        player.sendMessage(plugin.messageFormatter.format(message))
    }
    
    private fun sendActionBarNotification(player: Player, amount: Int) {
        val message = plugin.messagesManager.getMessage("pickup-actionbar", mapOf("amount" to amount.toString()))
        val formattedMessage = plugin.messageFormatter.format(message)
        
        // Convert legacy color codes to Component for ActionBar
        val component = Component.text(formattedMessage.replace("&", "§"))
        player.sendActionBar(component)
    }
    
    private fun sendBossBarNotification(player: Player, amount: Int) {
        // Remove existing boss bar if present
        activeBossBars[player]?.let { existingBar ->
            player.hideBossBar(existingBar)
            activeBossBars.remove(player)
        }
        
        val message = plugin.messagesManager.getMessage("pickup-bossbar", mapOf("amount" to amount.toString()))
        val formattedMessage = plugin.messageFormatter.format(message)
        
        // Parse boss bar color
        val color = try {
            BossBar.Color.valueOf(plugin.configManager.getBossbarColor().uppercase())
        } catch (e: IllegalArgumentException) {
            BossBar.Color.YELLOW
        }
        
        // Parse boss bar style
        val overlay = try {
            BossBar.Overlay.valueOf(plugin.configManager.getBossbarStyle().uppercase())
        } catch (e: IllegalArgumentException) {
            BossBar.Overlay.PROGRESS
        }
        
        val component = Component.text(formattedMessage.replace("&", "§"))
        val bossBar = BossBar.bossBar(component, 1.0f, color, overlay)
        
        player.showBossBar(bossBar)
        activeBossBars[player] = bossBar
        
        // Schedule boss bar removal
        val duration = plugin.configManager.getBossbarDuration()
        object : BukkitRunnable() {
            override fun run() {
                player.hideBossBar(bossBar)
                activeBossBars.remove(player)
            }
        }.runTaskLater(plugin, (duration * 20L)) // Convert seconds to ticks
    }
    
    private fun sendTitleNotification(player: Player, amount: Int) {
        val titleMessage = plugin.messagesManager.getMessage("pickup-title", mapOf("amount" to amount.toString()))
        val subtitleMessage = plugin.messagesManager.getMessage("pickup-subtitle", mapOf("amount" to amount.toString()))
        
        val formattedTitle = plugin.messageFormatter.format(titleMessage)
        val formattedSubtitle = plugin.messageFormatter.format(subtitleMessage)
        
        val titleComponent = Component.text(formattedTitle.replace("&", "§"))
        val subtitleComponent = Component.text(formattedSubtitle.replace("&", "§"))
        
        val fadeIn = Duration.ofMillis(plugin.configManager.getTitleFadeIn() * 50L) // Ticks to milliseconds
        val stay = Duration.ofMillis(plugin.configManager.getTitleStay() * 50L)
        val fadeOut = Duration.ofMillis(plugin.configManager.getTitleFadeOut() * 50L)
        
        val title = Title.title(
            titleComponent,
            subtitleComponent,
            Title.Times.times(fadeIn, stay, fadeOut)
        )
        
        player.showTitle(title)
    }
    
    fun clearPlayerNotifications(player: Player) {
        // Remove any active boss bars for the player
        activeBossBars[player]?.let { bossBar ->
            player.hideBossBar(bossBar)
            activeBossBars.remove(player)
        }
    }
    
    fun clearAllNotifications() {
        for ((player, bossBar) in activeBossBars) {
            player.hideBossBar(bossBar)
        }
        activeBossBars.clear()
    }
}
