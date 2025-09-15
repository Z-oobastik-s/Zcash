package com.zoobastiks.zcash.config

import com.zoobastiks.zcash.ZcashPlugin
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException

class MessagesManager(private val plugin: ZcashPlugin) {
    
    private lateinit var messagesFile: File
    private lateinit var messagesConfig: FileConfiguration
    
    fun loadMessages() {
        val language = plugin.configManager.getLanguage()
        val fileName = getMessagesFileName(language)
        
        messagesFile = File(plugin.dataFolder, fileName)
        
        // Save default files if they don't exist
        if (!messagesFile.exists()) {
            plugin.saveResource(fileName, false)
        }
        
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile)
    }
    
    private fun getMessagesFileName(language: String): String {
        return when (language.lowercase()) {
            "en" -> "messages_en.yml"
            "zh" -> "messages_zh.yml"
            "ru" -> "messages.yml"
            else -> "messages.yml" // Default to Russian
        }
    }
    
    fun changeLanguage(language: String) {
        // Update config
        plugin.configManager.setLanguage(language)
        // Reload messages with new language
        loadMessages()
    }
    
    fun getMessage(key: String): String {
        return messagesConfig.getString(key, "Message not found: $key") ?: "Message not found: $key"
    }
    
    fun getMessage(key: String, placeholders: Map<String, String>): String {
        var message = getMessage(key)
        for ((placeholder, value) in placeholders) {
            message = message.replace("{$placeholder}", value)
        }
        return message
    }
    
    fun reloadMessages() {
        loadMessages()
    }
    
    fun saveMessages() {
        try {
            messagesConfig.save(messagesFile)
        } catch (e: IOException) {
            plugin.logger.severe("Could not save messages.yml: ${e.message}")
        }
    }
}
