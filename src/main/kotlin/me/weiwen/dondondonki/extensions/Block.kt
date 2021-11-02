package me.weiwen.dondondonki.extensions

import org.bukkit.block.Block
import org.bukkit.block.data.Directional

val Block.blockBehind: Block?
    get() {
        val data = blockData as? Directional ?: return null
        return getRelative(data.facing.oppositeFace)
    }
val Block.blockInFront: Block?
    get() {
        val data = blockData as? Directional ?: return null
        return getRelative(data.facing)
    }
