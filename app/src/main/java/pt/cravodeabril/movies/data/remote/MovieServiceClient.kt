package pt.cravodeabril.movies.data.remote

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.basicAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import pt.cravodeabril.movies.data.ProblemDetails
import pt.cravodeabril.movies.data.Resource
import pt.cravodeabril.movies.data.local.entity.GenreEntity
import pt.cravodeabril.movies.utils.ByteArraySerializer
import pt.cravodeabril.movies.utils.InstantSerializer
import java.io.File
import kotlin.time.Instant

object MovieServiceClient {
    private val lock = Any()
    private var credentials: Credentials? = null


    fun setCredentials(username: String, password: String) = synchronized(lock) {
        this.credentials = Credentials(username, password)
    }

    fun getCredentials() = synchronized(lock) {
        credentials
    }

    private val client by lazy {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    serializersModule = SerializersModule {
                        contextual(InstantSerializer())
                        contextual(ByteArraySerializer())
                    }
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            defaultRequest {
                contentType(ContentType.Application.Json)
                getCredentials()?.run {
                    basicAuth(username, password)
                }
                url {
                    protocol = URLProtocol.HTTP
                    host = "10.0.2.2"
                    port = 8080
                }
            }
        }
    }

    fun getMovie(movieId: Long): Flow<Resource<MovieDetail>> = flow {
        emit(Resource.Loading)
        try {
            val response = client.get("/movies/$movieId")
            if (response.status.isSuccess()) {
                emit(Resource.Success(response.body()))
            } else {
                emit(Resource.Error(response.body()))
            }
        } catch (e: Exception) {
            emit(
                Resource.Error(
                    e, problem = ProblemDetails(
                        type = "Network",
                        title = "Network error",
                        status = 500,
                        detail = e.message ?: "Unknown"
                    )
                )
            )
        }
    }

    suspend fun getMoviePicture(
        movieId: Long, pictureId: Long
    ): Flow<Resource<FileRepresentation>>? {
        val response = client.get("/movies/$movieId/pictures/$pictureId")
        val bytes = response.body<ByteArray>()
        // FileOutputStream(file).use { it.write(bytes) }
        return null
    }

    suspend fun getMovies(
        offset: Long = 0,
        count: Int = Int.MAX_VALUE,
        director: Int? = null,
        genre: String? = null,
        title: String? = null,
        fromReleaseDate: LocalDate? = null,
        toReleaseDate: LocalDate? = null,
        fromRating: Int = 0,
        toRating: Int = 5,
        favoritesOnly: Boolean = false,
        sortBy: String = "releaseDate",
        sortOrder: String = "desc",
    ): Resource<List<MovieSimple>> {
        return try {
            val response = client.get("/movies") {
                parameter("offset", offset)
                parameter("count", count)
                director?.let { parameter("director", it) }
                genre?.let { parameter("genre", it) }
                title?.let { parameter("title", it) }
                fromReleaseDate?.let { parameter("fromReleaseDate", it) }
                toReleaseDate?.let { parameter("toReleaseDate", it) }
                parameter("fromRating", fromRating)
                parameter("toRating", toRating)
                parameter("favoritesOnly", favoritesOnly)
                parameter("sortBy", sortBy)
                parameter("sortOrder", sortOrder)
            }
            if (response.status.isSuccess()) {
                Resource.Success(response.body())
            } else {
                Resource.Error(response.body())
            }
        } catch (e: Exception) {
            Resource.Error(
                e, problem = ProblemDetails(
                    type = "Network",
                    title = "Network error",
                    status = 500,
                    detail = e.message ?: "Unknown"
                )
            )
        }
    }

    fun getMovieRatings(
        movieId: Long, sortBy: String = "desc", excludeUser: Int? = null
    ): Flow<Resource<List<MovieRating>>> = flow {
        emit(Resource.Loading)
        try {
            val response = client.get("/movies/$movieId/ratings") {
                parameter("sortBy", sortBy)
                excludeUser?.let { parameter("excludeUser", it) }
            }
            if (response.status.isSuccess()) {
                emit(Resource.Success(response.body()))
            } else {
                emit(Resource.Error(response.body()))
            }
        } catch (e: Exception) {
            emit(
                Resource.Error(
                    e, ProblemDetails(
                        type = "Network",
                        title = "Network error",
                        status = 500,
                        detail = e.message ?: "Unknown"
                    )
                )
            )
        }
    }

    suspend fun markAsFavorite(
        movieId: Long,
        value: Boolean
    ): Resource<Unit> {
        return try {
            val response = client.put("/movies/$movieId/mark-as-favorite") {
                parameter("value", value)
            }
            Log.d("BODY", response.toString())
            if (response.status.isSuccess()) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.body())
            }
        } catch (e: Exception) {
            Resource.Error(
                e,
                ProblemDetails(
                    type = "Network",
                    title = "Favorite failed",
                    status = 500,
                    detail = e.message ?: "Unknown"
                )
            )
        }
    }


    suspend fun createMovie(cmd: CreateMovieCommand): Resource<MovieDetail> {
        return try {
            val response = client.post("/movies") {
                setBody(cmd)
            }
            if (response.status.isSuccess()) Resource.Success(response.body())
            else Resource.Error(response.body())
        } catch (e: Exception) {
            Resource.Error(
                e, ProblemDetails("Network", "Create failed", 500, e.message ?: "")
            )
        }
    }

    suspend fun updateMovie(cmd: UpdateMovieCommand): Resource<MovieDetail> {
        return try {
            val response = client.put("/movies") {
                setBody(cmd)
            }
            if (response.status.isSuccess()) Resource.Success(response.body())
            else Resource.Error(response.body())
        } catch (e: Exception) {
            Resource.Error(
                e, ProblemDetails("Network", "Update failed", 500, e.message ?: "")
            )
        }
    }

    suspend fun deleteMovie(movieId: Long): Resource<Unit> {
        return try {
            val response = client.delete("/movies/$movieId")
            if (response.status.isSuccess()) Resource.Success(Unit)
            else Resource.Error(response.body())
        } catch (e: Exception) {
            Resource.Error(
                e, ProblemDetails("Network", "Delete failed", 500, e.message ?: "")
            )
        }
    }

    suspend fun login(username: String, password: String): Resource<LoginResponse> {
        return try {
            setCredentials(username, password)

            val response = client.get("/users/login")

            if (response.status.isSuccess()) {
                Resource.Success(response.body())
            } else {
                Resource.Error(response.body())
            }
        } catch (e: Exception) {
            Resource.Error(
                e, ProblemDetails(
                    type = "Network",
                    title = "Login failed",
                    status = 500,
                    detail = e.message ?: "Unknown"
                )
            )
        }
    }

    suspend fun getGenres(assignedOnly: Boolean?): Resource<List<Genre>> {
        return try {
            val response = client.get("/genres") {
                assignedOnly?.let { parameter("assignedOnly", it) }
            }
            if (response.status.isSuccess()) {
                Resource.Success(response.body())
            } else {
                Resource.Error(response.body())
            }
        } catch (e: Exception) {
            Resource.Error(
                e, problem = ProblemDetails(
                    type = "Network",
                    title = "Network error",
                    status = 500,
                    detail = e.message ?: "Unknown"
                )
            )
        }
    }

    suspend fun deleteGenre(id: Long): Resource<Unit> {
        return try {
            val response = client.delete("/genres/$id")
            if (response.status.isSuccess()) Resource.Success(Unit)
            else Resource.Error(response.body())
        } catch (e: Exception) {
            Resource.Error(
                e, ProblemDetails("Network", "Delete failed", 500, e.message ?: "")
            )
        }
    }

    suspend fun createGenres(cmd: CreateGenresCommand): Resource<List<Genre>> {
        return try {
            val response = client.post("/genres") {
                setBody(cmd.genres)
            }

            if (response.status.isSuccess()) Resource.Success(response.body())
            else Resource.Error(response.body())
        } catch (e: Exception) {
            Resource.Error(
                e, ProblemDetails("Network", "Create failed", 500, e.message ?: "")
            )
        }
    }

    suspend fun updateGenre(cmd: UpdateGenreCommand): Resource<Genre> {
        return try {
            val response = client.put("/genres") {
                setBody(cmd)
            }

            Log.v("UP", response.toString())
            if (response.status.isSuccess()) Resource.Success(response.body())
            else Resource.Error(response.body())
        } catch (e: Exception) {
            Resource.Error(
                e, ProblemDetails("Network", "Update failed", 500, e.message ?: "")
            )
        }
    }

    suspend fun getPeople(): Resource<List<PersonSimple>> {
        return try {
            val response = client.get("/people")
            if (response.status.isSuccess()) {
                Resource.Success(response.body())
            } else {
                Resource.Error(response.body())
            }
        } catch (e: Exception) {
            Resource.Error(
                e, problem = ProblemDetails(
                    type = "Network",
                    title = "Network error",
                    status = 500,
                    detail = e.message ?: "Unknown"
                )
            )
        }
    }

    suspend fun getPerson(id: Long): Resource<PersonDetail> {
        return try {
            val response = client.get("/people/$id")
            if (response.status.isSuccess()) {
                Resource.Success(response.body())
            } else {
                Resource.Error(response.body())
            }
        } catch (e: Exception) {
            Resource.Error(
                e, problem = ProblemDetails(
                    type = "Network",
                    title = "Network error",
                    status = 500,
                    detail = e.message ?: "Unknown"
                )
            )
        }
    }

    suspend fun deletePerson(id: Long): Resource<Unit> {
        return try {
            val response = client.delete("/people/$id")
            if (response.status.isSuccess()) Resource.Success(Unit)
            else Resource.Error(response.body())
        } catch (e: Exception) {
            Resource.Error(
                e, ProblemDetails("Network", "Delete failed", 500, e.message ?: "")
            )
        }
    }

    suspend fun createPerson(cmd: CreatePersonCommand): Resource<Person> {
        return try {
            val response = client.post("/people") {
                setBody(cmd)
            }

            if (response.status.isSuccess()) Resource.Success(response.body())
            else Resource.Error(response.body())
        } catch (e: Exception) {
            Resource.Error(
                e, ProblemDetails("Network", "Create failed", 500, e.message ?: "")
            )
        }
    }

    suspend fun updatePerson(cmd: UpdatePersonCommand): Resource<Person> {
        return try {
            val response = client.put("/people") {
                setBody(cmd)
            }
            if (response.status.isSuccess()) Resource.Success(response.body())
            else Resource.Error(response.body())
        } catch (e: Exception) {
            Resource.Error(
                e, ProblemDetails("Network", "Update failed", 500, e.message ?: "")
            )
        }
    }

    suspend fun createPersonPictures(cmd: AddPicturesToPersonCommand): Resource<Person> {
        return try {
            val response = client.put("/people/${cmd.personId}/add-pictures") {
                setBody(cmd.pictures)
            }
            if (response.status.isSuccess()) Resource.Success(response.body())
            else Resource.Error(response.body())
        } catch (e: Exception) {
            Resource.Error(
                e, ProblemDetails("Network", "Update failed", 500, e.message ?: "")
            )
        }
    }

    suspend fun removePersonPictures(cmd: RemovePicturesFromPersonCommand): Resource<Person> {
        return try {
            val response = client.put("/people/${cmd.personId}/add-pictures") {
                setBody(cmd.pictures.toIntArray())
            }
            if (response.status.isSuccess()) Resource.Success(response.body())
            else Resource.Error(response.body())
        } catch (e: Exception) {
            Resource.Error(
                e, ProblemDetails("Network", "Update failed", 500, e.message ?: "")
            )
        }
    }
}

data class Credentials(
    val username: String, val password: String
)

@Serializable
data class MovieSimple(
    val id: Int,
    val title: String,
    val synopsis: String,
    val genres: Set<String>,
    val releaseDate: LocalDate,
    val mainPicture: PictureInfo?,
    val favorite: Boolean,
    val director: Director?,
    val rating: Float?,
    val createdAt: Instant,
    val updatedAt: Instant?,
)

@Serializable
data class Director(val personId: Int, val name: String, val picture: PictureInfo?)

@Serializable
data class PictureInfo(
    val id: Int,
    val filename: String,
    val contentType: String,
    val mainPicture: Boolean,
    val description: String?
)

@Serializable
data class MovieDetail(
    val id: Int,
    val title: String,
    val synopsis: String,
    val genres: Set<Genre>,
    val releaseDate: LocalDate,
    val director: Director?,
    val pictures: List<PictureInfo>,
    val rating: Rating?,
    val favorite: Boolean = false,
    val userRating: UserRating? = null,
    val minimumAge: Int,
    val createdAt: Instant,
    val updatedAt: Instant?,
    val cast: List<CastMember>,
)

@Serializable
data class Genre(val id: Int, val name: String, val description: String?)

@Serializable
data class Rating(val average: Float, val buckets: List<RatingBucket>)

@Serializable
data class RatingBucket(val rating: Int, val count: Int)

@Serializable
data class UserRating(val rating: Int, val comment: String?)

@Serializable
data class CastMember(val personId: Int, val name: String, val character: String)

class FileRepresentation(val file: File, val filename: String, val contentType: String)

@Serializable
data class MovieRating(val movieId: Int, val userId: Int, val score: Int, val comment: String?)

@Serializable
data class CreateMovieCommand(
    val title: String,
    val synopsis: String,
    val cast: List<RoleAssignment>,
    val pictures: List<CreatePicture>,
    val genres: Set<Int>,
    val directorId: Int?,
    val releaseDate: LocalDate,
    val minimumAge: Int,
    val id: Int? = null
)

@Serializable
data class RoleAssignment(val personId: Int, val role: String)

@Serializable
class CreatePicture(
    val filename: String,
    val data: String /*ByteArray*/,
    val description: String? = null,
    val mainPicture: Boolean = false
)

@Serializable
data class UpdateMovieCommand(
    val id: Int,
    val title: String,
    val synopsis: String,
    val genres: Set<Int>,
    val directorId: Int?,
    val releaseDate: LocalDate,
    val minimumAge: Int = 0
)

@Serializable
data class CreateGenre(val name: String, val description: String?)

@Serializable
data class CreateGenresCommand(val genres: List<CreateGenre>)

@Serializable
class UpdateGenreCommand(val id: Int, val name: String, val description: String?)

@Serializable
class PersonSimple(val id: Int, val name: String, val dateOfBirth: LocalDate?, val picture: PictureInfo?)
@Serializable
class Person(val id: Int, val name: String, val dateOfBirth: LocalDate?, val pictures: List<PictureInfo>)
@Serializable
class PersonDetail(
    val id: Int,
    val name: String,
    val dateOfBirth: LocalDate?,
    val pictures: List<PictureInfo>,
    val directedMovies: List<Directed>,
    val roles: List<Role>
) {
    @Serializable
    class Directed(val id: Int, val title: String, val releaseDate: LocalDate, val picture: PictureInfo?)
    @Serializable
    class Role(val movieId: Int, val title: String, val releaseDate: LocalDate, val character: String)
}
@Serializable
data class CreatePersonCommand(val name: String, val dateOfBirth: LocalDate?, val pictures: List<CreatePicture>)
@Serializable
data class UpdatePersonCommand(val id: Int, val name: String, val dateOfBirth: LocalDate?)
@Serializable
data class AddPicturesToPersonCommand(val personId: Int, val pictures: List<CreatePicture>)
@Serializable
data class RemovePicturesFromPersonCommand(val personId: Int, val pictures: Set<Int>)