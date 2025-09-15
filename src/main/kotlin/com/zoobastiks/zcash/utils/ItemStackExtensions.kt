package com.zoobastiks.zcash.utils

import com.zoobastiks.zcash.ZcashPlugin
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

/**
 * Extension functions for ItemStack to work with currency data
 */

// NamespacedKey constants
private val CURRENCY_KEY by lazy { NamespacedKey(ZcashPlugin.instance, "currency") }
private val AMOUNT_KEY by lazy { NamespacedKey(ZcashPlugin.instance, "amount") }
private val SOURCE_KEY by lazy { NamespacedKey(ZcashPlugin.instance, "source") }

/**
 * Sets currency data to ItemStack
 */
fun ItemStack.setCurrencyData(amount: Int, source: String) {
    val meta = itemMeta ?: return
    meta.persistentDataContainer.apply {
        set(CURRENCY_KEY, PersistentDataType.BYTE, 1.toByte())
        set(AMOUNT_KEY, PersistentDataType.INTEGER, amount)
        set(SOURCE_KEY, PersistentDataType.STRING, source)
    }
    itemMeta = meta
}

/**
 * Gets currency amount from ItemStack
 */
fun ItemStack.getCurrencyAmount(): Int {
    return itemMeta?.persistentDataContainer?.get(AMOUNT_KEY, PersistentDataType.INTEGER) ?: 0
}

/**
 * Gets currency source from ItemStack
 */
fun ItemStack.getCurrencySource(): String {
    return itemMeta?.persistentDataContainer?.get(SOURCE_KEY, PersistentDataType.STRING) ?: "UNKNOWN"
}

/**
 * Checks if ItemStack is a currency item
 */
fun ItemStack.isCurrencyItem(): Boolean {
    return itemMeta?.persistentDataContainer?.has(CURRENCY_KEY, PersistentDataType.BYTE) == true
}
