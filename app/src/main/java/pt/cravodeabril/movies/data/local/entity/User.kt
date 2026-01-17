package pt.cravodeabril.movies.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Long,
    val username: String,
    val role: String,
    val dateOfBirth: LocalDate?,
    val createdAt: Instant,
    val updatedAt: Instant?
)
