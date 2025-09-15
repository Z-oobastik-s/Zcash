package com.zoobastiks.zcash.scheduler

import com.zoobastiks.zcash.ZcashPlugin
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class StatisticsResetScheduler(private val plugin: ZcashPlugin) {
    
    private var resetTask: BukkitRunnable? = null
    
    fun startScheduler() {
        stopScheduler() // Stop any existing scheduler
        
        if (!plugin.configManager.isStatisticsResetEnabled()) {
            return
        }
        
        val resetType = plugin.configManager.getStatisticsResetType()
        val resetTime = plugin.configManager.getStatisticsResetTime()
        
        val delayTicks = calculateDelayTicks(resetType, resetTime)
        val intervalTicks = calculateIntervalTicks(resetType)
        
        resetTask = object : BukkitRunnable() {
            override fun run() {
                if (plugin.configManager.isStatisticsResetEnabled()) {
                    plugin.databaseManager.resetAllStatistics()
                    
                    // Broadcast reset message to all players
                    val message = plugin.messagesManager.getMessage("statistics-reset-broadcast")
                    Bukkit.getOnlinePlayers().forEach { player ->
                        player.sendMessage(plugin.messageFormatter.format(message))
                    }
                    
                    plugin.logger.info("Statistics have been automatically reset (${resetType.lowercase()})")
                }
            }
        }
        
        resetTask?.runTaskTimerAsynchronously(plugin, delayTicks, intervalTicks)
        plugin.logger.info("Statistics reset scheduler started: ${resetType.lowercase()} at $resetTime")
    }
    
    fun stopScheduler() {
        resetTask?.cancel()
        resetTask = null
    }
    
    private fun calculateDelayTicks(resetType: String, resetTime: String): Long {
        val now = LocalDateTime.now()
        val targetTime = LocalTime.parse(resetTime) // Format: "HH:mm"
        
        val nextReset = when (resetType.uppercase()) {
            "DAILY" -> {
                val todayAtTargetTime = now.toLocalDate().atTime(targetTime)
                if (now.isAfter(todayAtTargetTime)) {
                    todayAtTargetTime.plusDays(1L)
                } else {
                    todayAtTargetTime
                }
            }
            "WEEKLY" -> {
                val todayAtTargetTime = now.toLocalDate().atTime(targetTime)
                val nextMonday = now.toLocalDate().plusDays((7 - now.dayOfWeek.value + 1).toLong())
                if (now.isAfter(todayAtTargetTime) && now.dayOfWeek.value == 1) {
                    // If it's Monday and past reset time, schedule for next Monday
                    nextMonday.atTime(targetTime)
                } else if (now.dayOfWeek.value == 1 && now.isBefore(todayAtTargetTime)) {
                    // If it's Monday and before reset time, schedule for today
                    todayAtTargetTime
                } else {
                    // Schedule for next Monday
                    nextMonday.atTime(targetTime)
                }
            }
            "MONTHLY" -> {
                val todayAtTargetTime = now.toLocalDate().atTime(targetTime)
                val firstOfNextMonth = now.toLocalDate().plusMonths(1L).withDayOfMonth(1)
                if (now.dayOfMonth == 1 && now.isBefore(todayAtTargetTime)) {
                    // If it's the 1st and before reset time, schedule for today
                    todayAtTargetTime
                } else {
                    // Schedule for the 1st of next month
                    firstOfNextMonth.atTime(targetTime)
                }
            }
            else -> {
                plugin.logger.warning("Unknown reset type: $resetType, defaulting to daily")
                val todayAtTargetTime = now.toLocalDate().atTime(targetTime)
                if (now.isAfter(todayAtTargetTime)) {
                    todayAtTargetTime.plusDays(1L)
                } else {
                    todayAtTargetTime
                }
            }
        }
        
        val secondsUntilReset = ChronoUnit.SECONDS.between(now, nextReset)
        return secondsUntilReset * 20L // Convert to ticks (20 ticks = 1 second)
    }
    
    private fun calculateIntervalTicks(resetType: String): Long {
        return when (resetType.uppercase()) {
            "DAILY" -> TimeUnit.DAYS.toSeconds(1) * 20L
            "WEEKLY" -> TimeUnit.DAYS.toSeconds(7) * 20L
            "MONTHLY" -> TimeUnit.DAYS.toSeconds(30) * 20L // Approximate
            else -> TimeUnit.DAYS.toSeconds(1) * 20L
        }
    }
}
