@file:OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
@file:UseContextualSerialization(Instant::class)

package pt.cravodeabril.movies.data.local.entity

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
data class User(
    val id: Int,
    val username: String,
    val passwordHash: String,
    val role: String,
    //val dateOfBirth: LocalDate?,
    val picture: String?,
    val createdAt: Instant,
    val updatedAt: Instant?
)

@Serializable
data class MovieRating(
    val userId: Int,
    val movieId: Int,
    val score: Int,
    val comment: String?,
    val createdAt: Instant,
    val updatedAt: Instant?
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
