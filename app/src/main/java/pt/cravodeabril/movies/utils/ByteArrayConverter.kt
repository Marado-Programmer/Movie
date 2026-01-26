@file:OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
@file:UseContextualSerialization(ByteArray::class)
package pt.cravodeabril.movies.utils

import androidx.room.TypeConverter
import io.ktor.util.encodeBase64
import kotlinx.serialization.KSerializer
import kotlinx.serialization.UseContextualSerialization
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.io.encoding.Base64
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

class ByteArrayConverter {
    @TypeConverter
    fun fromByteArray(value: ByteArray?): String? = value?.encodeBase64()

    @TypeConverter
    fun toByteArray(value: String?): ByteArray? = value?.let { Base64.decode(it) }
}

class ByteArraySerializer : KSerializer<ByteArray> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("ByteArray", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: ByteArray
    ) {
        encoder.encodeString(value.encodeBase64())
    }

    override fun deserialize(decoder: Decoder): ByteArray {
        val base64 = decoder.decodeString()
        return Base64.decode(base64)
    }
}
