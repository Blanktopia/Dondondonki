package me.weiwen.dondondonki.extensions

import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.Material.*
import org.bukkit.DyeColor.*
import org.bukkit.block.Block

val Material.isSign: Boolean
    get() = setOf(
        Material.ACACIA_WALL_SIGN,
        Material.BIRCH_WALL_SIGN,
        Material.DARK_OAK_WALL_SIGN,
        Material.JUNGLE_WALL_SIGN,
        Material.OAK_WALL_SIGN,
        Material.SPRUCE_WALL_SIGN,
        Material.CRIMSON_WALL_SIGN,
        Material.WARPED_WALL_SIGN,
    ).contains(this)

val Material.isContainer: Boolean
    get() = setOf(
        Material.BARREL,
        Material.CHEST,
        Material.SHULKER_BOX
    ).contains(this)
