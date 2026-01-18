@file:OptIn(ExperimentalUuidApi::class, ExperimentalTime::class) @file:UseContextualSerialization(
    Instant::class
)

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

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val synopsis: String,
    val releaseDate: LocalDate,
    val directorId: Long?,
    val rating: Float?,
    val minimumAge: Int?,
    val createdAt: Instant,
    val updatedAt: Instant?
)


@Entity(
    tableName = "pictures", foreignKeys = [ForeignKey(
        entity = MovieEntity::class,
        parentColumns = ["id"],
        childColumns = ["movieId"],
        onDelete = ForeignKey.CASCADE
    )], indices = [Index("movieId")]
)
data class PictureEntity(
    @PrimaryKey val id: Long,
    val movieId: Long,
    val filename: String,
    val contentType: String,
    val mainPicture: Boolean,
    val description: String?
)

@Entity(
    tableName = "movie_genres", primaryKeys = ["movieId", "genreId"], foreignKeys = [ForeignKey(
        entity = MovieEntity::class,
        parentColumns = ["id"],
        childColumns = ["movieId"],
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = GenreEntity::class,
        parentColumns = ["id"],
        childColumns = ["genreId"],
        onDelete = ForeignKey.CASCADE
    )], indices = [Index("movieId"), Index("genreId")]
)
data class MovieGenreCrossRef(
    val movieId: Long, val genreId: Long
)


@Entity(
    tableName = "cast_members", primaryKeys = ["movieId", "personId"]
)
data class CastMemberEntity(
    val movieId: Long, val personId: Long, val character: String
)

@Entity(
    tableName = "user_ratings", primaryKeys = ["userId", "movieId"], foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = MovieEntity::class,
        parentColumns = ["id"],
        childColumns = ["movieId"],
        onDelete = ForeignKey.CASCADE
    )], indices = [Index("movieId"), Index("userId")]
)
data class UserRatingEntity(
    val userId: Long, val movieId: Long, val rating: Int, val comment: String?
)

@Entity(
    tableName = "user_favorites", primaryKeys = ["userId", "movieId"], foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = MovieEntity::class,
        parentColumns = ["id"],
        childColumns = ["movieId"],
        onDelete = ForeignKey.CASCADE
    )], indices = [Index("movieId"), Index("userId")]
)
data class UserFavoriteEntity(
    val userId: Long, val movieId: Long
)

data class MovieWithDetails(
    @Embedded val movie: MovieEntity,

    @Relation(
        parentColumn = "id", entityColumn = "movieId"
    ) val pictures: List<PictureEntity>,


    @Relation(
        parentColumn = "id", entityColumn = "id", associateBy = Junction(
            value = MovieGenreCrossRef
            ::class, parentColumn = "movieId", entityColumn = "genreId"
        )
    ) val genres: List<GenreEntity>
)