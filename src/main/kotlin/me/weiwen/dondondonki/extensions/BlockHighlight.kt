/* Taken from https://github.com/sachingorkar102/Tweakin/blob/master/src/main/java/com/github/sachin/tweakin/reacharound/BlockHighLight.java */

package me.weiwen.dondondonki.extensions

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.utility.MinecraftReflection
import com.comphenix.protocol.wrappers.MinecraftKey
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import org.bukkit.Location
import org.bukkit.entity.Player
import java.lang.reflect.InvocationTargetException
import java.nio.charset.StandardCharsets

fun Player.highlightBlock(location: Location, color: UInt, duration: Int) {
    val packet = Unpooled.buffer()
    packet.writeLong(blockPosToLong(location.blockX, location.blockY, location.blockZ))
    packet.writeInt(color.toInt())
    writeString(packet, "")
    packet.writeInt(duration)
    sendPayload(this!!, "debug/game_test_add_marker", packet)
}

fun Player.clearHighlights() {
    val packet = Unpooled.buffer()
    sendPayload(this!!, "debug/game_test_clear", packet)
}

private fun sendPayload(receiver: Player, channel: String, bytes: ByteBuf) {
    val handle = PacketContainer(PacketType.Play.Server.CUSTOM_PAYLOAD)
    handle.minecraftKeys.write(0, MinecraftKey(channel))
    val serializer = MinecraftReflection.getPacketDataSerializer(bytes)
    handle.modifier.withType<Any>(ByteBuf::class.java).write(0, serializer)
    try {
        ProtocolLibrary.getProtocolManager().sendServerPacket(receiver, handle)
    } catch (e: InvocationTargetException) {
        throw RuntimeException("Unable to send the packet", e)
    }
}

private fun blockPosToLong(x: Int, y: Int, z: Int): Long {
    return x.toLong() and 67108863L shl 38 or (y.toLong() and 4095L) or (z.toLong() and 67108863L shl 12)
}

private fun d(packet: ByteBuf, i: Int) {
    var i = i
    while (i and -128 != 0) {
        packet.writeByte(i and 127 or 128)
        i = i ushr 7
    }
    packet.writeByte(i)
}

private fun writeString(packet: ByteBuf, s: String) {
    val abyte = s.toByteArray(StandardCharsets.UTF_8)
    d(packet, abyte.size)
    packet.writeBytes(abyte)
}