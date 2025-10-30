package com.zoobastiks.zcash

import com.zoobastiks.zcash.commands.ZcashCommand
import com.zoobastiks.zcash.config.ConfigManager
import com.zoobastiks.zcash.config.MessagesManager
import com.zoobastiks.zcash.database.DatabaseManager
import com.zoobastiks.zcash.economy.EconomyManager
import com.zoobastiks.zcash.listeners.BlockBreakListener
import com.zoobastiks.zcash.listeners.BlockPlaceListener
import com.zoobastiks.zcash.listeners.EntityDeathListener
import com.zoobastiks.zcash.listeners.InventoryPickupListener
import com.zoobastiks.zcash.listeners.PlayerPickupListener
import com.zoobastiks.zcash.listeners.PlayerQuitListener
import com.zoobastiks.zcash.scheduler.StatisticsResetScheduler
import com.zoobastiks.zcash.managers.CurrencyOptimizationManager
import com.zoobastiks.zcash.managers.NotificationManager
import com.zoobastiks.zcash.managers.PlacedBlocksManager
import com.zoobastiks.zcash.gui.LanguageGUI
import com.zoobastiks.zcash.utils.HologramManager
import com.zoobastiks.zcash.utils.MessageFormatter
import org.bukkit.ChatColor
import org.bukkit.plugin.java.JavaPlugin

class ZcashPlugin : JavaPlugin() {
    
    lateinit var configManager: ConfigManager
        private set
    
    lateinit var messagesManager: MessagesManager
        private set
    
    lateinit var economyManager: EconomyManager
        private set
    
    lateinit var messageFormatter: MessageFormatter
        private set
    
    lateinit var databaseManager: DatabaseManager
        private set
    
    lateinit var hologramManager: HologramManager
        private set
    
    lateinit var statisticsResetScheduler: StatisticsResetScheduler
        private set
    
    lateinit var currencyOptimizationManager: CurrencyOptimizationManager
        private set
    
    lateinit var notificationManager: NotificationManager
        private set
    
    lateinit var languageGUI: LanguageGUI
        private set
    
    lateinit var placedBlocksManager: PlacedBlocksManager
        private set
    
    companion object {
        lateinit var instance: ZcashPlugin
            private set
    }
    
    override fun onEnable() {
        instance = this
        
        // Initialize managers
        configManager = ConfigManager(this)
        messagesManager = MessagesManager(this)
        economyManager = EconomyManager(this)
        messageFormatter = MessageFormatter()
        databaseManager = DatabaseManager(this)
        hologramManager = HologramManager(this)
        statisticsResetScheduler = StatisticsResetScheduler(this)
        currencyOptimizationManager = CurrencyOptimizationManager(this)
        notificationManager = NotificationManager(this)
        languageGUI = LanguageGUI(this)
        placedBlocksManager = PlacedBlocksManager(this)
        
        // Load configurations
        configManager.loadConfig()
        messagesManager.loadMessages()
        
        // Initialize database
        databaseManager.initialize()
        
        // Initialize placed blocks manager (anti-duplication)
        placedBlocksManager.initialize()
        
        // Setup economy
        if (!economyManager.setupEconomy()) {
            logger.warning("Failed to setup economy! Vault or EssentialsX not found.")
        }
        
        // Start statistics reset scheduler
        statisticsResetScheduler.startScheduler()
        
        // Start currency optimization
        currencyOptimizationManager.startOptimization()
        
        // Register listeners
        registerListeners()
        
        // Register commands
        registerCommands()
        
        // Enable messages for console (hardcoded for security)
        val consoleMessages = listOf(
            "&a=========================================",
            "&6&l         ZCASH PLUGIN ENABLED",
            "&7          Version: &e1.0.0",
            "&7          Author: &bZoobastiks",
            "&7          Contact: &9https://t.me/Zoobastiks",
            "&a✓ &7Currency drops from mobs and blocks",
            "&a✓ &7Statistics tracking with auto-reset",
            "&a✓ &7Multiple notification types",
            "&a✓ &7Currency optimization and stacking",
            "&a✓ &7Vault/EssentialsX integration",
            "&a========================================="
        )
        
        consoleMessages.forEach { message ->
            server.consoleSender.sendMessage(
                ChatColor.translateAlternateColorCodes('&', message)
            )
        }
    }
    
    override fun onDisable() {
        // Stop schedulers and managers
        statisticsResetScheduler.stopScheduler()
        currencyOptimizationManager.stopOptimization()
        
        // Remove all holograms and notifications
        hologramManager.removeAllHolograms()
        notificationManager.clearAllNotifications()
        
        // Save and shutdown placed blocks manager
        placedBlocksManager.shutdown()
        
        // Close database connection
        databaseManager.close()
        
        // Disable messages for console (hardcoded for security)
        val disableMessages = listOf(
            "&c=========================================",
            "&4&l         ZCASH PLUGIN DISABLED",
            "&7Plugin has been safely disabled.",
            "&7Thanks for using Zcash by &bZoobastiks&7!",
            "&c========================================="
        )
        
        disableMessages.forEach { message ->
            server.consoleSender.sendMessage(
                ChatColor.translateAlternateColorCodes('&', message)
            )
        }
    }
    
    private fun registerListeners() {
        val pluginManager = server.pluginManager
        pluginManager.registerEvents(EntityDeathListener(this), this)
        pluginManager.registerEvents(BlockBreakListener(this), this)
        pluginManager.registerEvents(BlockPlaceListener(this), this)
        pluginManager.registerEvents(PlayerPickupListener(this), this)
        pluginManager.registerEvents(InventoryPickupListener(this), this)
        pluginManager.registerEvents(PlayerQuitListener(this), this)
        pluginManager.registerEvents(languageGUI, this)
    }
    
    private fun registerCommands() {
        getCommand("zcash")?.setExecutor(ZcashCommand(this))
        getCommand("zcash")?.tabCompleter = ZcashCommand(this)
    }
    
    fun reloadConfigs() {
        configManager.loadConfig()
        messagesManager.loadMessages()
        
        // Restart schedulers with new settings
        statisticsResetScheduler.startScheduler()
        currencyOptimizationManager.startOptimization()
    }
}
