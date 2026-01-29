@file:OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
@file:UseContextualSerialization(Instant::class)
package pt.cravodeabril.movies.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlinx.datetime.LocalDate
import kotlinx.serialization.UseContextualSerialization
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi

@Entity(tableName = "persons")
data class PersonEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val dateOfBirth: LocalDate?,
)

@Entity(
    tableName = "person_pictures", foreignKeys = [ForeignKey(
        entity = PersonEntity::class,
        parentColumns = ["id"],
        childColumns = ["personId"],
        onDelete = ForeignKey.CASCADE
    )], indices = [Index("personId")]
)
data class PersonPictureEntity(
    @PrimaryKey val id: Long,
    val personId: Long,
    val filename: String,
    val contentType: String,
    val mainPicture: Boolean,
    val description: String?
)

data class FullPersonEntity(
    @Embedded val person: PersonEntity,
    @Relation(
        parentColumn = "id", entityColumn = "personId"
    ) val pictures: List<PersonPictureEntity>
)