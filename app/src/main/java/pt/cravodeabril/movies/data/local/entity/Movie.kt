package pt.cravodeabril.movies.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlin.time.Instant

@Entity(tableName = "movies")
data class Movie(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val title: String,
    val synopsis: String,
    val minimumAge: Int,
    val director: Long?,
    val createdAt: Instant,
    val updatedAt: Instant?
)

@Entity(tableName = "movie_pictures")
data class MoviePicture(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val movieId: Long,
    val url: String,
    val filename: String,
    val contentType: String,
    val description: String?,
    val mainPicture: Boolean
)

@Entity(
    tableName = "movie_genres",
    primaryKeys = ["movieId", "genreId"],
    indices = [Index(value = ["genreId"])]
)
data class MovieGenre(
    val movieId: Long, val genreId: Long
)

@Entity(
    tableName = "cast_members", primaryKeys = ["movieId", "personId", "role"]
)
data class CastMember(
    val movieId: Long, val personId: Long, val role: String
)

data class MovieWithPictures(
    @Embedded val movie: Movie, @Relation(
        parentColumn = "id", entityColumn = "movieId"
    ) val pictures: List<MoviePicture>
)

data class MovieWithGenres(
    @Embedded val movie: Movie,

    @Relation(
        parentColumn = "id", entityColumn = "id", associateBy = androidx.room.Junction(
            value = MovieGenre::class, parentColumn = "movieId", entityColumn = "genreId"
        )
    ) val genres: List<Genre>
)

data class CastMemberWithPerson(
    val castMember: CastMember,
    val person: Person
)

data class MovieDetailsComposite(
    val movie: Movie,
    val pictures: List<MoviePicture>,
    val genres: List<Genre>,
    val castMembers: List<CastMemberWithPerson>
)

