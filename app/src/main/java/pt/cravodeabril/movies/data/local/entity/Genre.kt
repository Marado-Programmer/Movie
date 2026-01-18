@file:OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
@file:UseContextualSerialization(Instant::class)
package pt.cravodeabril.movies.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.UseContextualSerialization
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi

@Entity(tableName = "genres")
data class GenreEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val description: String?,
    val averageRating: Float
)