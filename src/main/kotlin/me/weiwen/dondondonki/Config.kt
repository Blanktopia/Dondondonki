@file:UseSerializers(TextColorSerializer::class)

package me.weiwen.dondondonki

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.*
import me.weiwen.dondondonki.serializers.TextColorSerializer
import net.kyori.adventure.text.format.TextColor
import org.bukkit.DyeColor
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.logging.Level

@Serializable
data class DondondonkiConfig(
    val namespace: String = "dondondonki",

    @SerialName("hook-essentials")
    val hookEssentials: Boolean = true,
    @SerialName("hook-moromoro")
    val hookMoromoro: Boolean = true,

    @SerialName("shop-line-1-color")
    val shopLine1Color: TextColor = TextColor.color(0xed4d2d),
    @SerialName("shop-line-2-color")
    val shopLine2Color: TextColor = TextColor.color(0xff8a72),
    @SerialName("shop-line-3-color")
    val shopLine3Color: TextColor = TextColor.color(0xff8a72),
    @SerialName("shop-line-4-color")
    val shopLine4Color: TextColor = TextColor.color(0xed4d2d),
    val shopGlowingColor: DyeColor = DyeColor.BROWN,

    @SerialName("donation-line-1-color")
    val donationLine1Color: TextColor = TextColor.color(0x2d89ed),
    @SerialName("donation-line-2-color")
    val donationLine2Color: TextColor = TextColor.color(0x72b6ff),
    @SerialName("donation-line-3-color")
    val donationLine3Color: TextColor = TextColor.color(0x72b6ff),
    @SerialName("donation-line-4-color")
    val donationLine4Color: TextColor = TextColor.color(0x2d89ed),
    val donationGlowingColor: DyeColor = DyeColor.BLUE,
)

fun parseConfig(plugin: JavaPlugin): DondondonkiConfig {
    val file = File(plugin.dataFolder, "config.yml")

    if (!file.exists()) {
        plugin.logger.log(Level.INFO, "Config file not found, creating default")
        plugin.dataFolder.mkdirs()
        file.createNewFile()
        file.writeText(Yaml().encodeToString(DondondonkiConfig()))
    }

    return try {
        Yaml().decodeFromString<DondondonkiConfig>(file.readText())
    } catch (e: Exception) {
        plugin.logger.log(Level.SEVERE, e.message)
        DondondonkiConfig()
    }
}