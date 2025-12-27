package pt.cravodeabril.movies.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "genres")
data class Genre(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val name: String,
    val description: String?
)