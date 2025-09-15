package com.zoobastiks.zcash.database

import com.zoobastiks.zcash.ZcashPlugin
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.CompletableFuture

class DatabaseManager(private val plugin: ZcashPlugin) {
    
    private val statisticsFile = File(plugin.dataFolder, "statistics.yml")
    private lateinit var statisticsConfig: FileConfiguration
    
    fun initialize() {
        try {
            if (!plugin.dataFolder.exists()) {
                plugin.dataFolder.mkdirs()
            }
            
            if (!statisticsFile.exists()) {
                statisticsFile.createNewFile()
            }
            
            statisticsConfig = YamlConfiguration.loadConfiguration(statisticsFile)
            
            plugin.logger.info("Statistics storage initialized successfully")
        } catch (e: Exception) {
            plugin.logger.severe("Failed to initialize statistics storage: ${e.message}")
            e.printStackTrace()
        }
    }
    
    fun addEarnings(player: Player, amount: Int, source: EarningSource) {
        CompletableFuture.runAsync {
            try {
                val uuid = player.uniqueId.toString()
                val currentMobs = statisticsConfig.getInt("players.$uuid.earned_from_mobs", 0)
                val currentBlocks = statisticsConfig.getInt("players.$uuid.earned_from_blocks", 0)
                val currentTotal = statisticsConfig.getInt("players.$uuid.total_earned", 0)
                
                statisticsConfig.set("players.$uuid.player_name", player.name)
                statisticsConfig.set("players.$uuid.last_updated", System.currentTimeMillis())
                
                when (source) {
                    EarningSource.MOB -> {
                        statisticsConfig.set("players.$uuid.earned_from_mobs", currentMobs + amount)
                    }
                    EarningSource.BLOCK -> {
                        statisticsConfig.set("players.$uuid.earned_from_blocks", currentBlocks + amount)
                    }
                }
                
                statisticsConfig.set("players.$uuid.total_earned", currentTotal + amount)
                saveStatistics()
            } catch (e: Exception) {
                plugin.logger.warning("Failed to add earnings to statistics: ${e.message}")
            }
        }
    }
    
    fun getPlayerStatistics(player: Player): PlayerStatistics? {
        return try {
            val uuid = player.uniqueId.toString()
            
            if (!statisticsConfig.contains("players.$uuid")) {
                return null
            }
            
            PlayerStatistics(
                playerName = statisticsConfig.getString("players.$uuid.player_name", player.name) ?: player.name,
                earnedFromMobs = statisticsConfig.getInt("players.$uuid.earned_from_mobs", 0),
                earnedFromBlocks = statisticsConfig.getInt("players.$uuid.earned_from_blocks", 0),
                totalEarned = statisticsConfig.getInt("players.$uuid.total_earned", 0),
                lastUpdated = statisticsConfig.getLong("players.$uuid.last_updated", 0).toString()
            )
        } catch (e: Exception) {
            plugin.logger.warning("Failed to get player statistics: ${e.message}")
            null
        }
    }
    
    fun resetAllStatistics() {
        CompletableFuture.runAsync {
            try {
                statisticsConfig.set("players", null)
                saveStatistics()
                plugin.logger.info("All statistics have been reset")
            } catch (e: Exception) {
                plugin.logger.warning("Failed to reset statistics: ${e.message}")
            }
        }
    }
    
    fun resetPlayerStatistics(playerUUID: UUID) {
        CompletableFuture.runAsync {
            try {
                statisticsConfig.set("players.${playerUUID}", null)
                saveStatistics()
            } catch (e: Exception) {
                plugin.logger.warning("Failed to reset player statistics: ${e.message}")
            }
        }
    }
    
    private fun saveStatistics() {
        try {
            statisticsConfig.save(statisticsFile)
        } catch (e: IOException) {
            plugin.logger.severe("Could not save statistics.yml: ${e.message}")
        }
    }
    
    fun close() {
        try {
            saveStatistics()
            plugin.logger.info("Statistics storage closed")
        } catch (e: Exception) {
            plugin.logger.warning("Error closing statistics storage: ${e.message}")
        }
    }
    
    enum class EarningSource {
        MOB, BLOCK
    }
    
    data class PlayerStatistics(
        val playerName: String,
        val earnedFromMobs: Int,
        val earnedFromBlocks: Int,
        val totalEarned: Int,
        val lastUpdated: String
    )
}
