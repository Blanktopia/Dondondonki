package me.weiwen.dondondonki

import me.weiwen.dondondonki.managers.*
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class Dondondonki: JavaPlugin() {
    companion object {
        lateinit var plugin: Dondondonki
            private set
    }

    var config: DondondonkiConfig = parseConfig(this)

    val itemParser: ItemParser by lazy { ItemParser(this) }
    val shopSignManager: ShopSignManager by lazy { ShopSignManager(this, itemParser) }
    val shopInventoryManager: ShopInventoryManager by lazy { ShopInventoryManager(this, itemParser) }

    override fun onLoad() {
        plugin = this
    }

    override fun onEnable() {
        itemParser.enable()
        shopSignManager.enable()
        shopInventoryManager.enable()

        getCommand("shop")?.apply {
            setExecutor { sender, _, _, args ->
                if (sender is Player) {
                    when (args[0]) {
                        "edit" -> {
                            if (shopInventoryManager.toggleBuyingFromTrustedShops(sender)) {
                                sender.sendMessage("You can now buy from trusted shops.")
                            } else {
                                sender.sendMessage("You can now edit trusted shops.")
                            }
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
            setTabCompleter { _, _, _, args ->
                when (args.size) {
                    0 -> listOf("edit")
                    else -> listOf()
                }
            }
        }

        getCommand("dondondonki")?.apply {
            setExecutor { sender, _, _, args ->
                when (args[0]) {
                    "reload" -> {
                        // TODO
                        sender.sendMessage(ChatColor.GOLD.toString() + "Reloaded configuration!")
                        true
                    }
                    else -> false
                }
            }
            setTabCompleter { _, _, _, args ->
                when (args.size) {
                    0 -> listOf("reload")
                    else -> listOf()
                }
            }
        }

        logger.info("Dondondonki is enabled")
    }

    override fun onDisable() {
        shopInventoryManager.disable()
        shopSignManager.disable()
        itemParser.disable()

        logger.info("Dondondonki is disabled")
    }
}
