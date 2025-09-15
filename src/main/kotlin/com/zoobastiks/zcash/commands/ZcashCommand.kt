package com.zoobastiks.zcash.commands

import com.zoobastiks.zcash.ZcashPlugin
import com.zoobastiks.zcash.utils.WorldManager
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil

class ZcashCommand(private val plugin: ZcashPlugin) : CommandExecutor, TabCompleter {
    
    private val subCommands = listOf("reload", "give", "stats", "language", "help")
    
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.isEmpty()) {
            sendHelp(sender)
            return true
        }
        
        when (args[0].lowercase()) {
            "reload" -> handleReload(sender)
            "give" -> handleGive(sender, args)
            "stats" -> handleStats(sender, args)
            "language" -> handleLanguage(sender)
            "help" -> sendHelp(sender)
            else -> {
                val message = plugin.messagesManager.getMessage("unknown-command")
                sender.sendMessage(plugin.messageFormatter.format(message))
            }
        }
        
        return true
    }
    
    private fun handleReload(sender: CommandSender) {
        if (!sender.hasPermission("zcash.reload")) {
            val message = plugin.messagesManager.getMessage("no-permission-reload")
            sender.sendMessage(plugin.messageFormatter.format(message))
            return
        }
        
        try {
            plugin.reloadConfigs()
            val message = plugin.messagesManager.getMessage("reload-success")
            sender.sendMessage(plugin.messageFormatter.format(message))
        } catch (e: Exception) {
            val message = plugin.messagesManager.getMessage("reload-error")
            sender.sendMessage(plugin.messageFormatter.format(message))
            plugin.logger.warning("Error reloading config: ${e.message}")
        }
    }
    
    private fun handleGive(sender: CommandSender, args: Array<String>) {
        if (!sender.hasPermission("zcash.give")) {
            val message = plugin.messagesManager.getMessage("no-permission-give")
            sender.sendMessage(plugin.messageFormatter.format(message))
            return
        }
        
        // Check world permissions for player senders
        if (sender is Player && !WorldManager.isEnabledInWorld(plugin, sender)) {
            return
        }
        
        if (args.size < 3) {
            val usageMessage = plugin.messagesManager.getMessage("give-usage")
            sender.sendMessage(plugin.messageFormatter.format(usageMessage))
            return
        }
        
        val targetPlayerName = args[1]
        val targetPlayer = Bukkit.getPlayer(targetPlayerName)
        
        if (targetPlayer == null) {
            val message = plugin.messagesManager.getMessage("give-player-not-found", mapOf(
                "player" to targetPlayerName
            ))
            sender.sendMessage(plugin.messageFormatter.format(message))
            return
        }
        
        val amount = args[2].toIntOrNull()
        if (amount == null || amount <= 0) {
            val message = plugin.messagesManager.getMessage("give-invalid-amount")
            sender.sendMessage(plugin.messageFormatter.format(message))
            return
        }
        
        // Give money through economy system
        val success = plugin.economyManager.giveMoney(targetPlayer, amount.toDouble())
        
        if (success) {
            // Send success message to sender
            val senderMessage = plugin.messagesManager.getMessage("give-success", mapOf(
                "amount" to amount.toString(),
                "player" to targetPlayer.name
            ))
            sender.sendMessage(plugin.messageFormatter.format(senderMessage))
            
            // Send notification to target player
            val targetMessage = plugin.messagesManager.getMessage("give-received", mapOf(
                "amount" to amount.toString()
            ))
            targetPlayer.sendMessage(plugin.messageFormatter.format(targetMessage))
        } else {
            val message = plugin.messagesManager.getMessage("economy-transaction-failed")
            sender.sendMessage(plugin.messageFormatter.format(message))
        }
    }
    
    private fun handleStats(sender: CommandSender, args: Array<String>) {
        if (!sender.hasPermission("zcash.stats")) {
            val message = plugin.messagesManager.getMessage("no-permission-stats")
            sender.sendMessage(plugin.messageFormatter.format(message))
            return
        }
        
        // Check world permissions for player senders
        if (sender is Player && !WorldManager.isEnabledInWorld(plugin, sender)) {
            return
        }
        
        val targetPlayer = if (args.size >= 2) {
            Bukkit.getPlayer(args[1])
        } else {
            if (sender is Player) sender else null
        }
        
        if (targetPlayer == null) {
            if (sender !is Player && args.size < 2) {
                val message = plugin.messagesManager.getMessage("console-cannot-use")
                sender.sendMessage(plugin.messageFormatter.format(message))
                return
            }
            
            val playerName = if (args.size >= 2) args[1] else "Unknown"
            val message = plugin.messagesManager.getMessage("give-player-not-found", mapOf(
                "player" to playerName
            ))
            sender.sendMessage(plugin.messageFormatter.format(message))
            return
        }
        
        // Get statistics from database
        val statistics = plugin.databaseManager.getPlayerStatistics(targetPlayer)
        
        val headerMessage = plugin.messagesManager.getMessage("stats-header", mapOf(
            "player" to targetPlayer.name
        ))
        sender.sendMessage(plugin.messageFormatter.format(headerMessage))
        
        if (statistics != null) {
            val mobsMessage = plugin.messagesManager.getMessage("stats-earned-mobs", mapOf(
                "amount" to statistics.earnedFromMobs.toString()
            ))
            sender.sendMessage(plugin.messageFormatter.format(mobsMessage))
            
            val blocksMessage = plugin.messagesManager.getMessage("stats-earned-blocks", mapOf(
                "amount" to statistics.earnedFromBlocks.toString()
            ))
            sender.sendMessage(plugin.messageFormatter.format(blocksMessage))
            
            val totalMessage = plugin.messagesManager.getMessage("stats-total-earned", mapOf(
                "amount" to statistics.totalEarned.toString()
            ))
            sender.sendMessage(plugin.messageFormatter.format(totalMessage))
        } else {
            val notFoundMessage = plugin.messagesManager.getMessage("stats-not-found", mapOf(
                "player" to targetPlayer.name
            ))
            sender.sendMessage(plugin.messageFormatter.format(notFoundMessage))
        }
    }
    
    private fun handleLanguage(sender: CommandSender) {
        if (sender !is Player) {
            val message = plugin.messagesManager.getMessage("console-cannot-use")
            sender.sendMessage(plugin.messageFormatter.format(message))
            return
        }
        
        // Check world permissions
        if (!WorldManager.isEnabledInWorld(plugin, sender)) {
            return
        }
        
        // Open language selection GUI
        plugin.languageGUI.openLanguageMenu(sender)
    }
    
    private fun sendHelp(sender: CommandSender) {
        val headerMessage = plugin.messagesManager.getMessage("help-header")
        sender.sendMessage(plugin.messageFormatter.format(headerMessage))
        
        val messages = listOf(
            plugin.messagesManager.getMessage("help-main"),
            plugin.messagesManager.getMessage("help-reload"),
            plugin.messagesManager.getMessage("help-give"),
            plugin.messagesManager.getMessage("help-stats"),
            plugin.messagesManager.getMessage("help-language"),
            plugin.messagesManager.getMessage("help-help")
        )
        
        messages.forEach { message ->
            sender.sendMessage(plugin.messageFormatter.format(message))
        }
    }
    
    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<String>): List<String> {
        val completions = mutableListOf<String>()
        
        when (args.size) {
            1 -> {
                // First argument - subcommands
                StringUtil.copyPartialMatches(args[0], subCommands, completions)
            }
            2 -> {
                // Second argument - player names for give and stats commands
                if (args[0].lowercase() in listOf("give", "stats")) {
                    val playerNames = Bukkit.getOnlinePlayers().map { it.name }
                    StringUtil.copyPartialMatches(args[1], playerNames, completions)
                }
            }
            3 -> {
                // Third argument - amount for give command
                if (args[0].lowercase() == "give") {
                    StringUtil.copyPartialMatches(args[2], listOf("1", "10", "100", "1000"), completions)
                }
            }
        }
        
        return completions.sorted()
    }
}
