package com.zoobastiks.zcash.managers

import com.zoobastiks.zcash.ZcashPlugin
import org.bukkit.Location
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.scheduler.BukkitRunnable
import java.io.File
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

/**
 * Optimized manager for tracking player-placed blocks to prevent coin duplication
 * Features:
 * - Configurable enable/disable
 * - Optional persistence across restarts
 * - Automatic cleanup of old blocks
 * - Memory-efficient with configurable limits
 */
class PlacedBlocksManager(private val plugin: ZcashPlugin) {
    
    // In-memory cache: block location -> timestamp when placed
    private val placedBlocks = ConcurrentHashMap<String, Long>()
    
    // File storage for persistence
    private val dataFile = File(plugin.dataFolder, "placed_blocks.yml")
    private lateinit var dataConfig: FileConfiguration
    
    // Auto-cleanup task
    private var cleanupTask: BukkitRunnable? = null
    
    fun initialize() {
        // Skip initialization if anti-duplication is disabled
        if (!plugin.configManager.isAntiDuplicationEnabled()) {
            plugin.logger.info("Anti-duplication system is DISABLED in config")
            return
        }
        
        try {
            // Create data folder if not exists
            if (!plugin.dataFolder.exists()) {
                plugin.dataFolder.mkdirs()
            }
            
            // Create file if not exists
            if (!dataFile.exists()) {
                dataFile.createNewFile()
            }
            
            // Load configuration
            dataConfig = YamlConfiguration.loadConfiguration(dataFile)
            
            // Load placed blocks from file into memory (if persistence enabled)
            if (plugin.configManager.shouldPersistPlacedBlocks()) {
                loadPlacedBlocks()
            }
            
            // Start automatic cleanup if enabled
            val cleanupInterval = plugin.configManager.getAutoCleanupInterval()
            if (cleanupInterval > 0) {
                startAutoCleanup()
            }
            
            plugin.logger.info("Anti-duplication system initialized with ${placedBlocks.size} tracked blocks")
            plugin.logger.info("Persistence: ${plugin.configManager.shouldPersistPlacedBlocks()}, " +
                             "Auto-cleanup: ${cleanupInterval > 0} (every ${cleanupInterval}min)")
        } catch (e: Exception) {
            plugin.logger.severe("Failed to initialize PlacedBlocksManager: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Marks a block as player-placed
     * This is a very fast O(1) operation
     */
    fun addPlacedBlock(location: Location) {
        // Skip if disabled
        if (!plugin.configManager.isAntiDuplicationEnabled()) return
        
        val key = getLocationKey(location)
        val currentTime = System.currentTimeMillis()
        
        placedBlocks[key] = currentTime
        
        // Check cache size and clean if necessary
        val maxSize = plugin.configManager.getMaxTrackedBlocks()
        if (placedBlocks.size > maxSize) {
            cleanOldestEntries()
        }
    }
    
    /**
     * Checks if a block was placed by a player
     * This is a very fast O(1) operation
     */
    fun isPlacedBlock(location: Location): Boolean {
        // If disabled, no blocks are tracked
        if (!plugin.configManager.isAntiDuplicationEnabled()) return false
        
        val key = getLocationKey(location)
        return placedBlocks.containsKey(key)
    }
    
    /**
     * Removes a block from tracking (when broken)
     * This is a very fast O(1) operation
     */
    fun removePlacedBlock(location: Location) {
        val key = getLocationKey(location)
        placedBlocks.remove(key)
    }
    
    /**
     * Converts location to unique string key
     * Format: "world:x:y:z"
     */
    private fun getLocationKey(location: Location): String {
        return "${location.world.name}:${location.blockX}:${location.blockY}:${location.blockZ}"
    }
    
    /**
     * Loads placed blocks from file with timestamps
     */
    private fun loadPlacedBlocks() {
        try {
            val section = dataConfig.getConfigurationSection("placed_blocks") ?: return
            placedBlocks.clear()
            
            var loadedCount = 0
            val currentTime = System.currentTimeMillis()
            val maxAge = plugin.configManager.getMaxBlockAge() * 60000L // Convert to milliseconds
            
            for (key in section.getKeys(false)) {
                val timestamp = section.getLong(key, currentTime)
                
                // Skip blocks that are too old
                if (maxAge > 0 && (currentTime - timestamp) > maxAge) {
                    continue
                }
                
                placedBlocks[key] = timestamp
                loadedCount++
            }
            
            plugin.logger.info("Loaded $loadedCount placed blocks from storage (skipped old entries)")
        } catch (e: Exception) {
            plugin.logger.warning("Failed to load placed blocks: ${e.message}")
        }
    }
    
    /**
     * Saves placed blocks to file with timestamps
     */
    fun savePlacedBlocks() {
        if (!plugin.configManager.shouldPersistPlacedBlocks()) return
        if (!plugin.configManager.isAntiDuplicationEnabled()) return
        
        try {
            // Clear old data
            dataConfig.set("placed_blocks", null)
            
            // Save only recent blocks to reduce file size
            val currentTime = System.currentTimeMillis()
            val maxAge = plugin.configManager.getMaxBlockAge() * 60000L
            
            var savedCount = 0
            for ((key, timestamp) in placedBlocks) {
                // Don't save very old blocks
                if (maxAge > 0 && (currentTime - timestamp) > maxAge) {
                    continue
                }
                
                dataConfig.set("placed_blocks.$key", timestamp)
                savedCount++
            }
            
            dataConfig.save(dataFile)
            plugin.logger.info("Saved $savedCount placed blocks to storage")
        } catch (e: IOException) {
            plugin.logger.severe("Could not save placed_blocks.yml: ${e.message}")
        }
    }
    
    /**
     * Starts automatic cleanup task
     */
    private fun startAutoCleanup() {
        stopAutoCleanup()
        
        val interval = plugin.configManager.getAutoCleanupInterval()
        if (interval <= 0) return
        
        cleanupTask = object : BukkitRunnable() {
            override fun run() {
                cleanExpiredBlocks()
            }
        }
        
        // Run every X minutes (convert to ticks: minutes * 60 seconds * 20 ticks)
        val ticks = interval * 60L * 20L
        cleanupTask?.runTaskTimerAsynchronously(plugin, ticks, ticks)
        
        plugin.logger.info("Started auto-cleanup task (every ${interval} minutes)")
    }
    
    /**
     * Stops automatic cleanup task
     */
    private fun stopAutoCleanup() {
        cleanupTask?.cancel()
        cleanupTask = null
    }
    
    /**
     * Cleans blocks older than max_block_age
     */
    private fun cleanExpiredBlocks() {
        try {
            val currentTime = System.currentTimeMillis()
            val maxAge = plugin.configManager.getMaxBlockAge() * 60000L // Convert to milliseconds
            
            if (maxAge <= 0) return
            
            val beforeSize = placedBlocks.size
            val iterator = placedBlocks.entries.iterator()
            
            while (iterator.hasNext()) {
                val entry = iterator.next()
                val age = currentTime - entry.value
                
                if (age > maxAge) {
                    iterator.remove()
                }
            }
            
            val removed = beforeSize - placedBlocks.size
            if (removed > 0) {
                plugin.logger.info("Auto-cleanup: removed $removed expired blocks (${placedBlocks.size} remaining)")
            }
        } catch (e: Exception) {
            plugin.logger.warning("Error during auto-cleanup: ${e.message}")
        }
    }
    
    /**
     * Cleans oldest entries when cache is full
     * Removes 20% of oldest blocks
     */
    private fun cleanOldestEntries() {
        try {
            val entriesToRemove = (placedBlocks.size * 0.2).toInt()
            
            // Sort by timestamp (oldest first)
            val sortedEntries = placedBlocks.entries
                .sortedBy { it.value }
                .take(entriesToRemove)
            
            // Remove oldest entries
            for (entry in sortedEntries) {
                placedBlocks.remove(entry.key)
            }
            
            plugin.logger.info("Cache limit reached: removed $entriesToRemove oldest blocks (${placedBlocks.size} remaining)")
        } catch (e: Exception) {
            plugin.logger.warning("Error cleaning oldest entries: ${e.message}")
        }
    }
    
    /**
     * Clears all tracked blocks
     */
    fun clearAll() {
        placedBlocks.clear()
        if (plugin.configManager.shouldPersistPlacedBlocks()) {
            dataConfig.set("placed_blocks", null)
            savePlacedBlocks()
        }
        plugin.logger.info("Cleared all placed blocks data")
    }
    
    /**
     * Gets count of tracked blocks
     */
    fun getTrackedBlocksCount(): Int = placedBlocks.size
    
    /**
     * Shutdown cleanup
     */
    fun shutdown() {
        try {
            stopAutoCleanup()
            
            if (plugin.configManager.isAntiDuplicationEnabled()) {
                savePlacedBlocks()
            }
            
            placedBlocks.clear()
            plugin.logger.info("PlacedBlocksManager shutdown complete")
        } catch (e: Exception) {
            plugin.logger.warning("Error during PlacedBlocksManager shutdown: ${e.message}")
        }
    }
    
    /**
     * Manual cleanup command (can be called via /zcash cleanup)
     */
    fun manualCleanup(): Int {
        val beforeSize = placedBlocks.size
        cleanExpiredBlocks()
        return beforeSize - placedBlocks.size
    }
}
