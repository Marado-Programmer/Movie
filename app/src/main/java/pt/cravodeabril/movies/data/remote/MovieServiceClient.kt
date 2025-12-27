package pt.cravodeabril.movies.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.basicAuth
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import pt.cravodeabril.movies.data.ApiResult
import pt.cravodeabril.movies.data.ProblemDetails
import pt.cravodeabril.movies.data.local.entity.Movie
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
        order: String = "desc",
        sortBy: String = "releaseDate",
        title: String? = null,
        genre: String? = null,
        favoritesOnly: Boolean = false
    ): Flow<ApiResult<List<MovieQueryResponse>>> = flow {
        emit(ApiResult.Loading)
        try {
            val response = client.get("/movies") {
                parameter("sortOrder", order)
                parameter("sortBy", sortBy)
                title?.let { parameter("title", it) }
                genre?.let { parameter("genre", it) }
                parameter("favoritesOnly", favoritesOnly)
            }

            if (response.status.isSuccess()) emit(ApiResult.Success(response.body()))
            else emit(ApiResult.Failure(response.body()))
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

    fun getMovieRatings(
        movieId: Int,
        sortBy: String = "desc",
        excludeUser: Int? = null
    ): Flow<ApiResult<List<MovieRatingResponse>>> = flow {
        emit(ApiResult.Loading)
        try {
            val response = client.get("/movies/$movieId/ratings") {
                parameter("sortBy", sortBy)
                excludeUser?.let { parameter("excludeUser", it) }
            }

            if (response.status.isSuccess()) emit(ApiResult.Success(response.body()))
            else emit(ApiResult.Failure(response.body()))
        } catch (e: Exception) {
            emit(ApiResult.Failure(
                ProblemDetails(
                    type = "Network",
                    title = "Network error",
                    status = 500,
                    detail = e.message ?: "Unknown"
                )
            ))
        }
    }
}


@Serializable
data class MovieQueryResponse(
    val id: Int,
    val title: String,
    val synopsis: String,
    val genres: List<String>,
    val releaseDate: String,
    val director: PersonResponse?,
    val mainPicture: PictureResponse?,
    val rating: Double,
    val favorite: Boolean,
    val createdAt: String,
    val updatedAt: String?
)


@Serializable
data class PictureResponse(
    val id: Int,
    val url: String,
    val filename: String,
    val contentType: String,
    val description: String?,
    val mainPicture: Boolean
)

@Serializable
data class PersonResponse(
    val id: Int, val name: String
)

fun MovieQueryResponse.toEntity() = Movie(
    id = id.toLong(),
    title = title,
    synopsis = synopsis,
    minimumAge = 0, // TODO: get from actual API response or default
    director = director?.id?.toLong(), // ← extract ID from PersonResponse
    createdAt = try {
        Instant.parse(createdAt)
    } catch (_: Exception) {
        Instant.DISTANT_PAST
    },
    updatedAt = updatedAt?.let {
        try {
            Instant.parse(it)
        } catch (_: Exception) {
            null
        }
    })

data class Credentials(
    val username: String,
    val password: String
)

@Serializable
data class MovieRatingResponse(
    val movieId: Int,
    val userId: Int,
    val score: Int,
    val comment: String?
)


