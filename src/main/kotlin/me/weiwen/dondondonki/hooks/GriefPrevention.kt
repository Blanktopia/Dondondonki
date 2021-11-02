package me.weiwen.dondondonki.hooks

import me.ryanhamshire.GriefPrevention.GriefPrevention
import org.bukkit.Location
import org.bukkit.entity.Player

fun Player.hasContainerTrust(location: Location): Boolean {
    val claim = GriefPrevention.instance.dataStore.getClaimAt(location, true, null) ?: return false
    return claim.allowContainers(this) == null
}
