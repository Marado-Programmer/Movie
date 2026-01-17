package pt.cravodeabril.movies.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.basicAuth
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.put
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
import pt.cravodeabril.movies.data.ApiResult
import pt.cravodeabril.movies.data.ProblemDetails
import java.io.File
import kotlin.time.Instant

object MovieServiceClient {
    private val lock = Any()
    private var credentials: Credentials? = null


    fun setCredentials(username: String, password: String) = synchronized(lock) {
        assert(username.isNotBlank()) { "username is blank" }
        assert(password.isNotBlank()) { "password is blank" }
        this.credentials = Credentials(username, password)
    }

    fun getCredentials() = synchronized(lock) {
        credentials
    }

    private val client by lazy {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
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

    fun getMovies(
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
    ): Flow<ApiResult<List<MovieSimple>>> = flow {
        emit(ApiResult.Loading)
        try {
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
                emit(ApiResult.Success(response.body()))
            } else {
                emit(ApiResult.Failure(response.body()))
            }
        } catch (e: Exception) {
            emit(
                ApiResult.Failure(
                    ProblemDetails(
                        type = "Network",
                        title = "Network error",
                        status = 500,
                        detail = e.message ?: "Unknown"
                    )
                )
            )
        }
    }

    fun getMovie(movieId: Long): Flow<ApiResult<MovieDetail>> = flow {
        emit(ApiResult.Loading)
        try {
            val response = client.get("/movies/$movieId")
            if (response.status.isSuccess()) {
                emit(ApiResult.Success(response.body()))
            } else {
                emit(ApiResult.Failure(response.body()))
            }
        } catch (e: Exception) {
            emit(
                ApiResult.Failure(
                    ProblemDetails(
                        type = "Network",
                        title = "Network error",
                        status = 500,
                        detail = e.message ?: "Unknown"
                    )
                )
            )
        }
    }

    suspend fun getMoviePicture(movieId: Long, pictureId: Long): Flow<ApiResult<FileRepresentation>>? {
        val response = client.get("/movies/$movieId/pictures/$pictureId")
        val bytes = response.body<ByteArray>()
        // FileOutputStream(file).use { it.write(bytes) }
        return null
    }

    fun getMovieRatings(
        movieId: Long, sortBy: String = "desc", excludeUser: Int? = null
    ): Flow<ApiResult<List<MovieRating>>> = flow {
        emit(ApiResult.Loading)
        try {
            val response = client.get("/movies/$movieId/ratings") {
                parameter("sortBy", sortBy)
                excludeUser?.let { parameter("excludeUser", it) }
            }
            if (response.status.isSuccess()) {
                emit(ApiResult.Success(response.body()))
            } else {
                emit(ApiResult.Failure(response.body()))
            }
        } catch (e: Exception) {
            emit(
                ApiResult.Failure(
                    ProblemDetails(
                        type = "Network",
                        title = "Network error",
                        status = 500,
                        detail = e.message ?: "Unknown"
                    )
                )
            )
        }
    }

    suspend fun markAsFavorite(movieId: Long, value: Boolean) {
        client.put("/movies/$movieId/mark-as-favorite") {
            parameter("value", value)
        }
    }
}

data class Credentials(
    val username: String, val password: String
)

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

data class Director(val personId: Int, val name: String, val picture: PictureInfo?)

data class PictureInfo(
    val id: Int,
    val filename: String,
    val contentType: String,
    val mainPicture: Boolean,
    val description: String?
)

data class MovieDetail(
    val id: Int,
    val title: String,
    val synopsis: String,
    val genres: Set<Genre>,
    val releaseDate: LocalDate,
    val director: Director?,
    val pictures: List<PictureInfo>,
    val rating: Rating?,
    val favorite: Boolean,
    val userRating: UserRating?,
    val minimumAge: Int,
    val createdAt: Instant,
    val updatedAt: Instant?,
    val cast: List<CastMember>,
)

data class Genre(val id: Int, val name: String, val description: String?)

@Serializable
data class Rating(val average: Float, val buckets: List<RatingBucket>)

@Serializable
data class RatingBucket(val rating: Int, val count: Int)

data class UserRating(val rating: Int, val comment: String?)

data class CastMember(val personId: Int, val name: String, val character: String)

class FileRepresentation(val file: File, val filename: String, val contentType: String)

@Serializable
data class MovieRating(val movieId: Int, val userId: Int, val score: Int, val comment: String?)