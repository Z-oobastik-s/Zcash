package com.zoobastiks.zcash.config

import com.zoobastiks.zcash.ZcashPlugin
import org.bukkit.Material
import org.bukkit.configuration.file.FileConfiguration

class ConfigManager(private val plugin: ZcashPlugin) {
    
    private lateinit var config: FileConfiguration
    
    fun loadConfig() {
        plugin.saveDefaultConfig()
        plugin.reloadConfig()
        config = plugin.config
    }
    
    fun getConfig(): FileConfiguration = config
    
    // Economy settings
    fun useVault(): Boolean = config.getBoolean("economy.use_vault", true)
    fun fallbackToEssentials(): Boolean = config.getBoolean("economy.fallback_to_essentials", true)
    
    // General settings
    fun getLanguage(): String = config.getString("general.language", "ru") ?: "ru"
    fun setLanguage(language: String) {
        config.set("general.language", language)
        plugin.saveConfig()
    }
    fun getDefaultAmount(): String = config.getString("general.default_amount", "1-2") ?: "1-2"
    fun getDefaultChance(): Int = config.getInt("general.default_chance", 100)
    fun getUnknownMobAmount(): Int = config.getInt("general.unknown_mob_amount", 1)
    
    // World settings
    fun getWorldMode(): String = config.getString("worlds.mode", "all") ?: "all"
    fun getWorldList(): List<String> = config.getStringList("worlds.list")
    fun shouldShowDisabledMessages(): Boolean = config.getBoolean("worlds.show_disabled_messages", false)
    
    fun isWorldEnabled(worldName: String): Boolean {
        val mode = getWorldMode()
        val worldList = getWorldList()
        
        return when (mode.lowercase()) {
            "whitelist" -> worldList.contains(worldName)
            "blacklist" -> !worldList.contains(worldName)
            "all" -> true
            else -> true // Default to enabled if mode is unknown
        }
    }
    
    // Visual settings
    fun getDisplayItem(): Material {
        val itemName = config.getString("visual.display_item", "GOLD_NUGGET") ?: "GOLD_NUGGET"
        return try {
            Material.valueOf(itemName)
        } catch (e: IllegalArgumentException) {
            Material.GOLD_NUGGET
        }
    }
    
    // Pickup settings
    fun allowHopperPickup(): Boolean = config.getBoolean("pickup.allow_hopper_pickup", false)
    fun allowHopperMinecartPickup(): Boolean = config.getBoolean("pickup.allow_hopper_minecart_pickup", false)
    
    // Commands on pickup
    fun isCommandsEnabled(): Boolean = config.getBoolean("enable-commands", true)
    fun getPickupCommands(): List<String> = config.getStringList("commands.pickup")
    
    // Mob settings
    fun getMobAmount(mobType: String): String {
        return config.getString("mobs.$mobType.amount", getDefaultAmount()) ?: getDefaultAmount()
    }
    
    fun getMobChance(mobType: String): Int {
        return config.getInt("mobs.$mobType.chance", getDefaultChance())
    }
    
    // Block settings
    fun getBlockAmount(blockType: String): String {
        return config.getString("blocks.$blockType.amount", getDefaultAmount()) ?: getDefaultAmount()
    }
    
    fun getBlockChance(blockType: String): Int {
        return config.getInt("blocks.$blockType.chance", getDefaultChance())
    }
    
    // Visual settings
    fun isHologramEnabled(): Boolean = config.getBoolean("visual.show_hologram", true)
    fun getHologramHeight(): Double = config.getDouble("visual.hologram_height", 0.5)
    
    // Sound settings
    fun isPickupSoundEnabled(): Boolean = config.getBoolean("sounds.pickup_enabled", true)
    fun getPickupSound(): String = config.getString("sounds.pickup_sound", "minecraft:entity.axolotl.swim") ?: "minecraft:entity.axolotl.swim"
    fun getPickupVolume(): Float = config.getDouble("sounds.pickup_volume", 1.0).toFloat()
    fun getPickupPitch(): Float = config.getDouble("sounds.pickup_pitch", 1.0).toFloat()
    
    // Statistics settings
    fun isStatisticsResetEnabled(): Boolean = config.getBoolean("statistics.reset_enabled", true)
    fun getStatisticsResetType(): String = config.getString("statistics.reset_type", "DAILY") ?: "DAILY"
    fun getStatisticsResetTime(): String = config.getString("statistics.reset_time", "00:00") ?: "00:00"
    
    // Optimization settings
    fun isStackCurrencyEnabled(): Boolean = config.getBoolean("optimization.stack_currency", true)
    fun getStackRadius(): Double = config.getDouble("optimization.stack_radius", 3.0)
    fun getDespawnTime(): Int = config.getInt("optimization.despawn_time", 300)
    fun getHideDistance(): Double = config.getDouble("optimization.hide_distance", 15.0)
    
    // Performance settings
    fun getMaxItemsPerTick(): Int = config.getInt("optimization.performance.max_items_per_tick", 50)
    fun getMaxStackChecks(): Int = config.getInt("optimization.performance.max_stack_checks", 10)
    fun getMaxStackSize(): Int = config.getInt("optimization.performance.max_stack_size", 5)
    fun getWarningThreshold(): Int = config.getInt("optimization.performance.warning_threshold", 200)
    
    // Notification settings
    fun getPickupNotificationType(): String = config.getString("notifications.pickup_type", "ACTIONBAR") ?: "ACTIONBAR"
    fun getBossbarDuration(): Int = config.getInt("notifications.bossbar_duration", 3)
    fun getBossbarColor(): String = config.getString("notifications.bossbar_color", "YELLOW") ?: "YELLOW"
    fun getBossbarStyle(): String = config.getString("notifications.bossbar_style", "SOLID") ?: "SOLID"
    fun getTitleFadeIn(): Int = config.getInt("notifications.title_fade_in", 10)
    fun getTitleStay(): Int = config.getInt("notifications.title_stay", 40)
    fun getTitleFadeOut(): Int = config.getInt("notifications.title_fade_out", 10)
    
    // Anti-duplication settings
    fun isAntiDuplicationEnabled(): Boolean = config.getBoolean("optimization.anti_duplication.enabled", true)
    fun shouldPersistPlacedBlocks(): Boolean = config.getBoolean("optimization.anti_duplication.persist_data", false)
    fun getMaxTrackedBlocks(): Int = config.getInt("optimization.anti_duplication.max_tracked_blocks", 50000)
    fun getAutoCleanupInterval(): Int = config.getInt("optimization.anti_duplication.auto_cleanup_interval", 120)
    fun getMaxBlockAge(): Int = config.getInt("optimization.anti_duplication.max_block_age", 240)
    
    // Helper method to parse amount ranges
    fun parseAmount(amountString: String): Int {
        return if (amountString.contains("-")) {
            val parts = amountString.split("-")
            if (parts.size == 2) {
                val min = parts[0].toIntOrNull() ?: 1
                val max = parts[1].toIntOrNull() ?: 1
                (min..max).random()
            } else {
                1
            }
        } else {
            amountString.toIntOrNull() ?: 1
        }
    }
}
