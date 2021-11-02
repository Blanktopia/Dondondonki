package me.weiwen.dondondonki.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.kyori.adventure.text.format.TextColor

class TextColorSerializer : KSerializer<TextColor> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("Color", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): TextColor {
        val string = decoder.decodeString()

        return TextColor.fromHexString(string) ?: TextColor.color(0xffffff)
    }

    override fun serialize(encoder: Encoder, value: TextColor) {
        val string = value.toString()

        val hex = value.asHexString()

        return encoder.encodeString(string)
    }
}
