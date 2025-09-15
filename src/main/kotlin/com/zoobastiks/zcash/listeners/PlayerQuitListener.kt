package com.zoobastiks.zcash.listeners

import com.zoobastiks.zcash.ZcashPlugin
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PlayerQuitListener(private val plugin: ZcashPlugin) : Listener {
    
    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        // Clear any active notifications for the leaving player
        plugin.notificationManager.clearPlayerNotifications(event.player)
    }
}
