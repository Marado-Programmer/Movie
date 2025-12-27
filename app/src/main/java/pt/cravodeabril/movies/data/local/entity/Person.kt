package pt.cravodeabril.movies.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "persons")
data class Person(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val name: String,
    //val dateOfBirth: LocalDate?
)