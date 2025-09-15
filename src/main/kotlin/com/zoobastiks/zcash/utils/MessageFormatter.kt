package com.zoobastiks.zcash.utils

import net.md_5.bungee.api.ChatColor
import java.awt.Color
import java.util.regex.Pattern

class MessageFormatter {
    
    private val gradientPattern = Pattern.compile("<gradient:(#[a-fA-F0-9]{6}):(#[a-fA-F0-9]{6})>(.*?)</gradient>")
    private val hexPattern = Pattern.compile("&#([a-fA-F0-9]{6})")
    
    fun format(message: String): String {
        var formatted = message
        
        // Process gradients first
        formatted = processGradients(formatted)
        
        // Process individual hex colors
        formatted = processHexColors(formatted)
        
        // Process legacy color codes
        formatted = ChatColor.translateAlternateColorCodes('&', formatted)
        
        return formatted
    }
    
    private fun processGradients(message: String): String {
        val matcher = gradientPattern.matcher(message)
        val result = StringBuffer()
        
        while (matcher.find()) {
            val startColor = matcher.group(1)
            val endColor = matcher.group(2)
            val text = matcher.group(3)
            
            val gradientText = createGradient(text, startColor, endColor)
            matcher.appendReplacement(result, gradientText)
        }
        
        matcher.appendTail(result)
        return result.toString()
    }
    
    private fun processHexColors(message: String): String {
        val matcher = hexPattern.matcher(message)
        val result = StringBuffer()
        
        while (matcher.find()) {
            val hexCode = matcher.group(1)
            val chatColor = ChatColor.of("#$hexCode")
            matcher.appendReplacement(result, chatColor.toString())
        }
        
        matcher.appendTail(result)
        return result.toString()
    }
    
    private fun createGradient(text: String, startHex: String, endHex: String): String {
        if (text.isEmpty()) return ""
        
        val startColor = Color.decode(startHex)
        val endColor = Color.decode(endHex)
        
        val length = text.length
        val result = StringBuilder()
        
        for (i in text.indices) {
            val ratio = if (length == 1) 0.0 else i.toDouble() / (length - 1)
            
            val red = (startColor.red + ratio * (endColor.red - startColor.red)).toInt()
            val green = (startColor.green + ratio * (endColor.green - startColor.green)).toInt()
            val blue = (startColor.blue + ratio * (endColor.blue - startColor.blue)).toInt()
            
            val interpolatedColor = Color(red, green, blue)
            val hexString = String.format("#%02x%02x%02x", interpolatedColor.red, interpolatedColor.green, interpolatedColor.blue)
            
            result.append(ChatColor.of(hexString))
            result.append(text[i])
        }
        
        return result.toString()
    }
    
    fun stripColors(message: String): String {
        return ChatColor.stripColor(message) ?: message
    }
}
