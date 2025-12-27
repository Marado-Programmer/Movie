@file:OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
@file:UseContextualSerialization(Instant::class)

package pt.cravodeabril.movies.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi

@Serializable
data class ProblemDetails(
    val type: String,
    val title: String,
    val status: Int,
    val detail: String,
    val instance: String? = null
)

class InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: Instant
    ) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Instant {
        val dateTime8601 = decoder.decodeString()
        return Instant.parse(dateTime8601)
    }
}