package me.weiwen.dondondonki.managers

import com.earth2me.essentials.Essentials
import me.weiwen.dondondonki.Dondondonki
import me.weiwen.dondondonki.extensions.*
import me.weiwen.dondondonki.hooks.hasContainerTrust
import org.bukkit.*
import org.bukkit.block.Container
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.*
import org.bukkit.inventory.BlockInventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*
import java.util.logging.Level

class ShopInventoryManager(val plugin: Dondondonki, val itemParser: ItemParser) : Listener {
    val isBuyingFromTrustedShops: MutableSet<UUID> = mutableSetOf()

    companion object {
        lateinit var manager: ShopInventoryManager
    }

    fun enable() {
        manager = this
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    fun disable() {}

    fun toggleBuyingFromTrustedShops(player: Player): Boolean {
        if (isBuyingFromTrustedShops.contains(player.uniqueId)) {
            isBuyingFromTrustedShops.remove(player.uniqueId)
            return false
        } else {
            isBuyingFromTrustedShops.add(player.uniqueId)
            return true
        }
    }

    @EventHandler(ignoreCancelled = true)
    private fun onInventoryOpen(event: InventoryOpenEvent) {
        val block = (event.inventory.holder as? BlockInventoryHolder)?.block ?: return
        val chest = block.state as? Container ?: return
        val container = chest.persistentDataContainer
        val type = container.get(NamespacedKey(plugin.config.namespace, "type"), PersistentDataType.STRING) ?: return

        if (type == "shop") {
            val owner = container.get(NamespacedKey(plugin.config.namespace, "owner"), PersistentDataType.STRING)
            val player = event.player as? Player ?: return

            if (!player.hasPermission("blanktopia.shop.buy")) {
                player.sendMessage("${ChatColor.RED}You don't have permission to buy from shops!")
                event.isCancelled = true
                return
            }

            if (!isBuyingFromTrustedShops.contains(player.uniqueId) &&
                ((owner != null && UUID.fromString(owner) == player.uniqueId) ||
                        player.hasContainerTrust(block.location))
            ) {
                    player.sendActionBar("${ChatColor.GOLD}Type /shop edit to buy from shops you are trusted in.")
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private fun onInventoryDrag(event: InventoryDragEvent) {
        val block = (event.inventory.holder as? BlockInventoryHolder)?.block ?: return
        val chest = block.state as? Container ?: return
        val container = chest.persistentDataContainer
        val type = container.get(NamespacedKey(plugin.config.namespace, "type"), PersistentDataType.STRING) ?: return

        if (type == "shop") {
            val owner = container.get(NamespacedKey(plugin.config.namespace, "owner"), PersistentDataType.STRING)
            val player = event.whoClicked as? Player ?: return

            if (!isBuyingFromTrustedShops.contains(player.uniqueId) &&
                ((owner != null && UUID.fromString(owner) == player.uniqueId) ||
                        player.hasContainerTrust(block.location))
            ) {
                return
            }

            event.isCancelled = true

        } else if (type == "donation") {
            val owner = container.get(NamespacedKey(plugin.config.namespace, "owner"), PersistentDataType.STRING)
            val player = event.whoClicked as? Player ?: return
            if ((owner != null && UUID.fromString(owner) == player.uniqueId) || player.hasContainerTrust(block.location)) {
                return
            }
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private fun onInventoryClick(event: InventoryClickEvent) {
        val block = (event.inventory.holder as? BlockInventoryHolder)?.block ?: return
        val chest = block.state as? Container ?: return
        val container = chest.persistentDataContainer
        val type = container.get(NamespacedKey(plugin.config.namespace, "type"), PersistentDataType.STRING) ?: return
        if (type == "shop") {
            val owner = container.get(NamespacedKey(plugin.config.namespace, "owner"), PersistentDataType.STRING)
            val player = event.whoClicked as? Player ?: return

            if (!isBuyingFromTrustedShops.contains(player.uniqueId) &&
                ((owner != null && UUID.fromString(owner) == player.uniqueId) ||
                        player.hasContainerTrust(block.location))
            ) {
                return
            }

            event.isCancelled = true

            if (event.click != ClickType.LEFT) return
            val inventory = event.clickedInventory ?: return
            if (inventory.type == InventoryType.PLAYER) return

            val price = container.get(NamespacedKey(plugin.config.namespace, "price"), PersistentDataType.BYTE_ARRAY)?.let {
                ItemStack.deserializeBytes(it)
            }

            val clickedItem = inventory.getItem(event.slot) ?: return

            if (price != null) {
                if (itemParser.isSameItem(clickedItem, price)) return

                if (!player.inventory.containsAtLeast(price.asOne(), price.amount)) {
                    player.playSoundTo(Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, SoundCategory.PLAYERS, 1.0f, 1.0f)
                    return
                }
            }

            val emptySlot = player.inventory.firstEmpty()
            if (emptySlot == -1) {
                player.playSoundTo(Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, SoundCategory.PLAYERS, 1.0f, 1.0f)
                return
            }

            if (price != null) {
                val didntRemove = player.inventory.removeItem(price)
                if (didntRemove.size != 0) {
                    player.playSoundTo(Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, SoundCategory.PLAYERS, 1.0f, 1.0f)
                    return
                }
            }

            player.inventory.setItem(emptySlot, clickedItem)
            player.playSoundAt(Sound.BLOCK_NOTE_BLOCK_BELL, SoundCategory.PLAYERS, 1.0f, 0.81f)

            if (owner != null) {
                inventory.setItem(event.slot, price)
                logPurchase(player, UUID.fromString(owner), clickedItem, price)
            } else {
                val price = if (price != null) { "${price.amount} ${itemParser.name(price)}" } else { "FREE" }
                plugin.logger.log(
                    Level.INFO,
                    "${ChatColor.GOLD}${player.displayName}${ChatColor.GOLD} has bought ${itemParser.name(clickedItem)}${ChatColor.GOLD} for $price${ChatColor.GOLD}."
                )
            }
        } else if (type == "donation") {
            val owner = container.get(NamespacedKey(plugin.config.namespace, "owner"), PersistentDataType.STRING) ?: return
            val player = event.whoClicked as? Player ?: return

            if (!isBuyingFromTrustedShops.contains(player.uniqueId) &&
                ((owner != "" && UUID.fromString(owner) == player.uniqueId) ||
                        player.hasContainerTrust(block.location))
            ) {
                return
            }

            val inventory = event.clickedInventory ?: return
            if (inventory.type == InventoryType.PLAYER
                && (event.click != ClickType.SHIFT_LEFT || event.click != ClickType.SHIFT_RIGHT)
            ) {
                return
            }

            // Allow only if:
            // - non-empty cursor
            // - left click
            // - on an empty slot
            val item = event.cursor
            val clickedItem = inventory.getItem(event.slot)
            if (item == null
                || item.type == Material.AIR
                || event.click != ClickType.LEFT
                || (clickedItem != null && clickedItem.type != Material.AIR)
            ) {
                player.playSoundTo(Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, SoundCategory.PLAYERS, 1.0f, 1.0f)
                event.isCancelled = true
                return
            }

            player.playSoundAt(Sound.BLOCK_NOTE_BLOCK_BELL, SoundCategory.PLAYERS, 1.0f, 0.81f)

            if (owner != null) {
                logMail(player, UUID.fromString(owner), item)
            } else {
                plugin.logger.log(
                    Level.INFO,
                    "${ChatColor.GOLD}${player.displayName}${ChatColor.GOLD} has sent ${itemParser.name(item)}${ChatColor.GOLD}."
                )
            }
        }
    }

    private fun logPurchase(player: Player, uuid: UUID, clickedItem: ItemStack, price: ItemStack?) {
        val owner = Bukkit.getServer().getOfflinePlayer(uuid)
        val boughtItem = itemParser.name(clickedItem)
        val price = if (price != null) { "${price.amount} ${itemParser.name(price)}" } else { "FREE" }
        player.sendMessage("${ChatColor.GOLD}You have bought ${boughtItem}${ChatColor.GOLD} for ${price}.${ChatColor.GOLD}")

        val essentials = plugin.server.pluginManager.getPlugin("Essentials") as? Essentials ?: return
        val user = essentials.getUser(owner.uniqueId) ?: return
        plugin.logger.log(
            Level.INFO,
            "${ChatColor.GOLD}${player.displayName}${ChatColor.GOLD} has bought ${boughtItem}${ChatColor.GOLD} from ${owner.name} for ${price}${ChatColor.GOLD}."
        )
        if (!owner.isOnline || user.isAfk) {
            user.addMail("${ChatColor.GOLD}${player.displayName}${ChatColor.GOLD} has bought ${boughtItem}${ChatColor.GOLD} for ${price}${ChatColor.GOLD}.")
        } else {
            owner.player?.let {
                it.sendMessage("${ChatColor.GOLD}${player.displayName}${ChatColor.GOLD} has bought ${boughtItem}${ChatColor.GOLD} for ${price}.${ChatColor.GOLD}.")
                it.playSoundTo(Sound.BLOCK_NOTE_BLOCK_BELL, SoundCategory.PLAYERS, 1.0f, 0.81f)
            }
        }
    }

    private fun logMail(player: Player, uuid: UUID, item: ItemStack) {
        val owner = Bukkit.getServer().getOfflinePlayer(uuid)
        val price = "${item.amount} ${itemParser.name(item)}"
        player.sendMessage("${ChatColor.GOLD}You have sent ${price}${ChatColor.GOLD}")

        val essentials = plugin.server.pluginManager.getPlugin("Essentials") as? Essentials ?: return
        val user = essentials.getUser(owner.uniqueId) ?: return
        plugin.logger.log(
            Level.INFO,
            "${ChatColor.GOLD}${player.displayName}${ChatColor.GOLD} has sent ${price}${ChatColor.GOLD} to ${owner.name}."
        )
        if (!owner.isOnline || user.isAfk) {
            user.addMail("${ChatColor.GOLD}${player.displayName}${ChatColor.GOLD} has sent you ${price}${ChatColor.GOLD}.")
        } else {
            owner.player?.let {
                it.sendMessage("${ChatColor.GOLD}${player.displayName}${ChatColor.GOLD} has sent you ${price}${ChatColor.GOLD}.")
                it.playSoundTo(Sound.BLOCK_NOTE_BLOCK_BELL, SoundCategory.PLAYERS, 1.0f, 0.81f)
            }
        }
    }
}