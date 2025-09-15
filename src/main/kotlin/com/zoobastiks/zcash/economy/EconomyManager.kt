package com.zoobastiks.zcash.economy

import com.zoobastiks.zcash.ZcashPlugin
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.RegisteredServiceProvider

class EconomyManager(private val plugin: ZcashPlugin) {
    
    private var economy: Economy? = null
    private var vaultAvailable = false
    
    fun setupEconomy(): Boolean {
        if (!plugin.configManager.useVault()) {
            plugin.logger.info("Vault integration disabled in config")
            return setupEssentialsXFallback()
        }
        
        val vaultPlugin = plugin.server.pluginManager.getPlugin("Vault")
        if (vaultPlugin == null || !vaultPlugin.isEnabled) {
            plugin.logger.warning("Vault not found, trying EssentialsX fallback")
            return setupEssentialsXFallback()
        }
        
        val rsp: RegisteredServiceProvider<Economy>? = plugin.server.servicesManager.getRegistration(Economy::class.java)
        if (rsp == null) {
            plugin.logger.warning("No economy provider found, trying EssentialsX fallback")
            return setupEssentialsXFallback()
        }
        
        economy = rsp.provider
        vaultAvailable = true
        plugin.logger.info("Vault economy integration enabled")
        return true
    }
    
    private fun setupEssentialsXFallback(): Boolean {
        if (!plugin.configManager.fallbackToEssentials()) {
            plugin.logger.warning("EssentialsX fallback disabled in config")
            return false
        }
        
        val essentialsPlugin = plugin.server.pluginManager.getPlugin("Essentials")
        if (essentialsPlugin == null || !essentialsPlugin.isEnabled) {
            plugin.logger.severe("Neither Vault nor EssentialsX found! Economy features disabled.")
            return false
        }
        
        plugin.logger.info("Using EssentialsX fallback for economy")
        return true
    }
    
    fun isEconomyAvailable(): Boolean {
        return vaultAvailable || isEssentialsXAvailable()
    }
    
    private fun isEssentialsXAvailable(): Boolean {
        val essentials = plugin.server.pluginManager.getPlugin("Essentials")
        return essentials != null && essentials.isEnabled
    }
    
    fun giveMoney(player: Player, amount: Double): Boolean {
        if (vaultAvailable && economy != null) {
            return try {
                val response = economy!!.depositPlayer(player, amount)
                response.transactionSuccess()
            } catch (e: Exception) {
                plugin.logger.warning("Vault transaction failed: ${e.message}")
                fallbackToEssentials(player, amount)
            }
        }
        
        return fallbackToEssentials(player, amount)
    }
    
    private fun fallbackToEssentials(player: Player, amount: Double): Boolean {
        if (!isEssentialsXAvailable()) {
            return false
        }
        
        return try {
            // Execute eco give command through console
            val command = "eco give ${player.name} $amount"
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)
            true
        } catch (e: Exception) {
            plugin.logger.warning("EssentialsX fallback failed: ${e.message}")
            false
        }
    }
    
    fun getBalance(player: Player): Double {
        if (vaultAvailable && economy != null) {
            return try {
                economy!!.getBalance(player)
            } catch (e: Exception) {
                plugin.logger.warning("Failed to get balance from Vault: ${e.message}")
                0.0
            }
        }
        
        // EssentialsX doesn't have a simple API for getting balance without reflection
        // So we'll return 0.0 as a fallback
        return 0.0
    }
    
    fun executePickupCommands(player: Player, amount: Int) {
        val commands = plugin.configManager.getPickupCommands()
        
        for (command in commands) {
            try {
                val processedCommand = command
                    .replace("{player}", player.name)
                    .replace("{amount}", amount.toString())
                    .replace("{nbt}", "") // Placeholder for NBT data if needed
                
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand)
            } catch (e: Exception) {
                plugin.logger.warning("Failed to execute pickup command '$command': ${e.message}")
            }
        }
    }
}
