package me.weiwen.dondondonki.managers

import com.earth2me.essentials.Essentials
import me.weiwen.dondondonki.Dondondonki
import me.weiwen.moromoro.Moromoro
import me.weiwen.moromoro.extensions.customItemKey
import me.weiwen.moromoro.items.ItemManager
import me.weiwen.moromoro.items.item
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class ItemParser(val plugin: Dondondonki) {
    var essentials: Essentials? = null
    var moromoro: Moromoro? = null

    fun enable() {
        if (plugin.config.hookEssentials && plugin.server.pluginManager.isPluginEnabled("Essentials")) {
            essentials = plugin.server.pluginManager.getPlugin("Essentials") as Essentials
        }

        if (plugin.config.hookMoromoro && plugin.server.pluginManager.isPluginEnabled("Moromoro")) {
            moromoro = plugin.server.pluginManager.getPlugin("Moromoro") as Moromoro
        }
    }

    fun disable() {
    }

    fun name(item: ItemStack): String {
        val name = if (moromoro != null && item.customItemKey != null) {
            ItemManager.templates[item.customItemKey]?.name?.value?.let {
                ChatColor.stripColor(it)
            } ?: item.i18NDisplayName ?: item.type.toString()
        } else {
            item.i18NDisplayName ?: item.type.toString()
        }

        if (name.startsWith("Block of ")) {
            return "${name.substring(9)} Block"
        }

        return name
    }

    fun parse(str: String): ItemStack? {
        val split = str.split(" ", limit = 2)
        if (split.size == 1) {
            return parse(str, 1)
        }

        val amount = split[0].toIntOrNull() ?: return null
        if (amount > 64) {
            return null
        }

        val name = split[1]

        return parse(name, amount)
    }

    private fun singularize(name: String): String {
        var replaced = name.replace(Regex("ies$"), "y")
        if (replaced != name) return replaced
        replaced = name.replace(Regex("(s|c|ch|sh)es$"), "$1")
        if (replaced != name) return replaced
        replaced = name.replace(Regex("s$"), "")
        return replaced
    }

    private fun parse(name: String, amount: Int): ItemStack? {
        val name = singularize(name)

        var item: ItemStack? = null

        if (moromoro != null) {
            item = parseMoromoro(name)
        }

        if (item == null && essentials != null) {
            item = parseEssentials(name.lowercase().replace(" ", "_"))
        }

        if (item == null) {
            Material.matchMaterial(name.lowercase().replace(" ", "_"))
                ?: Material.getMaterial(name.uppercase().replace(" ", "_"))
        }

        item?.amount = amount

        return item
    }

    private fun parseEssentials(string: String): ItemStack? {
        return try {
            essentials?.itemDb?.get(string)
        } catch (_: Exception) {
            null
        }
    }

    private fun parseMoromoro(string: String): ItemStack? {
        val regex = Regex("[^a-zA-Z ]")
        return ItemManager.templates.firstNotNullOfOrNull { (key, template) ->
            if (ChatColor.stripColor(template.name?.value)?.replace(regex, "")?.trim()?.lowercase()
                    ?.equals(string) == true
            ) {
                template.item(key, 1)
            } else {
                null
            }
        }
    }

    fun isSameItem(a: ItemStack, b: ItemStack): Boolean {
        if (a.type != b.type) {
            return false
        }
        if (moromoro?.let { moromoro ->
                val at = a.itemMeta.persistentDataContainer.get(
                    NamespacedKey(moromoro.config.namespace, "type"),
                    PersistentDataType.STRING
                )
                val bt = b.itemMeta.persistentDataContainer.get(
                    NamespacedKey(moromoro.config.namespace, "type"),
                    PersistentDataType.STRING
                )
                at != bt
            } == true) {
            return false
        }
        return true
    }
}