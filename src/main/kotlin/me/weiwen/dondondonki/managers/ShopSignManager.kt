package me.weiwen.dondondonki.managers

import me.weiwen.dondondonki.Dondondonki
import me.weiwen.dondondonki.extensions.blockBehind
import me.weiwen.dondondonki.extensions.blockInFront
import me.weiwen.dondondonki.extensions.isContainer
import me.weiwen.dondondonki.extensions.isSign
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.block.Chest
import org.bukkit.block.Sign
import org.bukkit.block.Container
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

class ShopSignManager(val plugin: Dondondonki, val itemParser: ItemParser) : Listener {
    companion object {
        lateinit var manager: ShopSignManager
    }

    fun enable() {
        manager = this
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    fun disable() {}

    private fun createShop(lines: List<Component>, player: Player, sign: Block, chest: Block): Boolean {
        val chestState = chest.state as? Container ?: return false

        val lines = lines.map {
            PlainTextComponentSerializer.plainText().serialize(it)
        }

        if (!player.hasPermission("dondondonki.shop.create")) {
            player.sendMessage("${ChatColor.RED}You don't have permission to set up shops.")
            return false
        }

        if (!chest.type.isContainer) {
            player.sendMessage("${ChatColor.RED}The sign has to be placed on a chest, barrel, or shulker box.")
            return false
        }

        val description = lines[1]

        val price = itemParser.parse(lines[2])
        if (price == null && lines[2].lowercase() != "free") {
            player.sendMessage("${ChatColor.RED}Put the price in the third line. e.g. \"1 diamond\"")
            return false
        }

        val (owner, ownerName) = when (lines[3]) {
            "" -> {
                Pair(player.uniqueId, player.name)
            }
            "admin" -> {
                if (!player.hasPermission("dondondonki.shop.create.admin")) {
                    player.sendMessage("${ChatColor.RED}Leave the last line blank.")
                    return false
                }
                Pair(null, "")
            }
            else -> {
                if (!player.hasPermission("dondondonki.shop.create.others")) {
                    player.sendMessage("${ChatColor.RED}Leave the last line blank.")
                    return false
                }
                Pair(plugin.server.getOfflinePlayer(lines[3]).uniqueId, lines[3])
            }
        }

        chestState.customName = if (price != null) {
            "${price.amount} ${itemParser.name(price)} per stack"
        } else {
            "FREE"
        }
        chestState.update()

        registerShop(sign, chest, description, price, owner, ownerName)

        return true
    }

    private fun registerShop(sign: Block, chest: Block, description: String, price: ItemStack?, owner: UUID?, ownerName: String) {
        val chestState = chest.state as? Container ?: return
        val pdc = chestState.persistentDataContainer

        pdc.set(NamespacedKey(plugin.config.namespace, "type"), PersistentDataType.STRING, "shop")
        owner?.let {
            pdc.set(NamespacedKey(plugin.config.namespace, "owner"), PersistentDataType.STRING, it.toString())
        }
        price?.let {
            pdc.set(
                NamespacedKey(plugin.config.namespace, "price"),
                PersistentDataType.BYTE_ARRAY,
                it.serializeAsBytes()
            )
        }
        chestState.update()

        val signState = sign.state as? Sign ?: return
        signState.line(0, Component.text("[Shop]").color(plugin.config.shopLine1Color))
        signState.line(1, Component.text(description).color(plugin.config.shopLine2Color).decorate(TextDecoration.BOLD))
        signState.line(2,
            Component.text(
                if (price != null) { "${price.amount} ${itemParser.name(price)}" } else { "FREE" }
            ).color(plugin.config.shopLine3Color)
        )
        signState.line(3, Component.text(ownerName).color(plugin.config.shopLine4Color))
        signState.color = plugin.config.shopGlowingColor
        signState.isGlowingText = true
        signState.isEditable = false
        signState.update()

        // TODO: add to shop registry
    }

    private fun createDonationBox(lines: List<Component>, player: Player, sign: Block, chest: Block): Boolean {
        val lines = lines.map {
            PlainTextComponentSerializer.plainText().serialize(it)
        }

        if (!player.hasPermission("dondondonki.donation.create")) {
            player.sendMessage("${ChatColor.RED}You don't have permission to set up donation boxes.")
            return false
        }

        if (!chest.type.isContainer) {
            player.sendMessage("${ChatColor.RED}The sign has to be placed on a chest, barrel, or shulker box.")
            return false
        }

        val (owner, ownerName) = when (lines[3]) {
            "" -> {
                Pair(player.uniqueId, player.name)
            }
            "admin" -> {
                if (!player.hasPermission("dondondonki.donation.create.admin")) {
                    player.sendMessage("${ChatColor.RED}Leave the last line blank.")
                    return false
                }
                Pair(null, "")
            }
            else -> {
                if (!player.hasPermission("dondondonki.donation.create.others")) {
                    player.sendMessage("${ChatColor.RED}Leave the last line blank.")
                    return false
                }
                Pair(plugin.server.getOfflinePlayer(lines[3]).uniqueId, lines[3])
            }
        }

        registerDonationBox(sign, chest, lines, owner, ownerName)

        return true
    }

    private fun registerDonationBox(sign: Block, chest: Block, lines: List<String>, owner: UUID?, ownerName: String) {
        val chestState = chest.state as? Container ?: return
        val pdc = chestState.persistentDataContainer

        pdc.set(NamespacedKey(plugin.config.namespace, "type"), PersistentDataType.STRING, "donation")
        owner?.let {
            pdc.set(NamespacedKey(plugin.config.namespace, "owner"), PersistentDataType.STRING, it.toString())
        }
        chestState.update()

        val signState = sign.state as? Sign ?: return
        signState.line(0, Component.text("[Donation]").color(plugin.config.donationLine1Color))
        signState.line(1, Component.text(lines[1]).color(plugin.config.donationLine2Color))
        signState.line(2, Component.text(lines[2]).color(plugin.config.donationLine3Color))
        signState.line(3, Component.text(ownerName).color(plugin.config.donationLine4Color))
        signState.color = plugin.config.donationGlowingColor
        signState.isGlowingText = true
        signState.isEditable = false
        signState.update()

        // TODO: add to donation registry
    }

    private fun migrateBlanktopiaShop(sign: Block, chest: Block) {
        val chestState = chest.state as? Container ?: return
        val pdc = chestState.persistentDataContainer
        var type = pdc.get(NamespacedKey("blanktopiashop", "type"), PersistentDataType.STRING)

        // Migrate chest type
        if (type == null && pdc.get(NamespacedKey("blanktopiashop", "cost"), PersistentDataType.STRING) != null) {
            pdc.set(NamespacedKey("blanktopiashop", "type"), PersistentDataType.STRING, "shop")
            type = "shop"
        }

        if (type == "shop") {
            val signState = sign.state as? Sign ?: return

            val amount = pdc.get(NamespacedKey("blanktopiashop", "amount"), PersistentDataType.INTEGER) ?: return
            val item = pdc.get(NamespacedKey("blanktopiashop", "item"), PersistentDataType.STRING) ?: return
            val price = if (amount != 0) {
                ItemStack(Material.valueOf(item), amount)
            } else {
                null
            }
            if (price != null) {
                pdc.set(NamespacedKey(plugin.config.namespace, "price"), PersistentDataType.BYTE_ARRAY, price.serializeAsBytes())
            }

            var owner: String? = pdc.get(NamespacedKey("blanktopiashop", "owner"), PersistentDataType.STRING) ?: return
            if (owner?.isEmpty() == true) {
                owner = null
            }
            val ownerName = PlainTextComponentSerializer.plainText().serialize(signState.line(3))

//            pdc.remove(NamespacedKey("blanktopiashop", "type"))
//            pdc.remove(NamespacedKey("blanktopiashop", "owner"))
//            pdc.remove(NamespacedKey("blanktopiashop", "amount"))
//            pdc.remove(NamespacedKey("blanktopiashop", "item"))
//            pdc.remove(NamespacedKey("blanktopiashop", "cost"))
//            chestState.update()

            val description = PlainTextComponentSerializer.plainText().serialize(signState.line(1))
            registerShop(sign, chest, description, price, owner?.let { UUID.fromString(it) }, ownerName)

        } else if (type == "mail") {
            val signState = sign.state as? Sign ?: return

            var owner: String? = pdc.get(NamespacedKey("blanktopiashop", "owner"), PersistentDataType.STRING) ?: return
            if (owner?.isEmpty() == true) {
                owner = null
            }
            val ownerName = PlainTextComponentSerializer.plainText().serialize(signState.line(3))

//            pdc.remove(NamespacedKey("blanktopiashop", "type"))
//            pdc.remove(NamespacedKey("blanktopiashop", "owner"))
//            chestState.update()

            registerDonationBox(sign, chest, signState.lines().map {
                PlainTextComponentSerializer.plainText().serialize(it)
            }, owner?.let { UUID.fromString(it) }, ownerName)
        }
    }

    @EventHandler(ignoreCancelled = false)
    fun onRightClickShop(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return

        val block = event.clickedBlock ?: return

        val player = event.player

        val chest = if (block.type.isSign) {
            block.blockBehind ?: return
        } else if (block.type.isContainer) {
            block
        } else {
            return
        }

        val state = chest.state as? Container ?: return
        val pdc = state.persistentDataContainer

        if (pdc.has(NamespacedKey(plugin.config.namespace, "type"), PersistentDataType.STRING)) {
            player.openInventory(state.inventory)
        } else if (pdc.has(NamespacedKey("blanktopiashop", "type"), PersistentDataType.STRING)) {
            if (block.type.isSign) {
                migrateBlanktopiaShop(block, chest)
            }
        }
    }

    @EventHandler
    fun onSignChange(event: SignChangeEvent) {
        val sign = event.block
        val line = PlainTextComponentSerializer.plainText().serialize(
            event.line(0) ?: return
        ).lowercase()

        when (line) {
            "[shop]" -> {
                val chest = event.block.blockBehind ?: return
                event.isCancelled = true
                if (createShop(event.lines(), event.player, sign, chest)) {
                } else {
                    sign.breakNaturally()
                    return
                }
            }
            "[mail]", "[donation]" -> {
                val chest = event.block.blockBehind ?: return
                event.isCancelled = true
                if (createDonationBox(event.lines(), event.player, sign, chest)) {
                } else {
                    sign.breakNaturally()
                    return
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent) {
        if (!event.block.type.isSign) return
        val chest = event.block.blockBehind ?: return
        val state = chest.state as? Container ?: return
        val container = state.persistentDataContainer
        val uuid = container.get(NamespacedKey(plugin.config.namespace, "owner"), PersistentDataType.STRING)
        if (uuid != null && UUID.fromString(uuid) != event.player.uniqueId &&
            !event.player.hasPermission("dondondonki.shop.break")
        ) {
            event.player.sendMessage("${ChatColor.RED}You don't have permission to break other players' shops!")
            event.isCancelled = true
            return
        }
        container.remove(NamespacedKey(plugin.config.namespace, "type"))
        container.remove(NamespacedKey(plugin.config.namespace, "owner"))
        container.remove(NamespacedKey(plugin.config.namespace, "price"))

        state.customName = null
        state.update()
    }
}
