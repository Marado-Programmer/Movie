package pt.cravodeabril.movies.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pt.cravodeabril.movies.data.ApiResult
import pt.cravodeabril.movies.data.local.dao.MovieDao
import pt.cravodeabril.movies.data.local.entity.Genre
import pt.cravodeabril.movies.data.local.entity.Movie
import pt.cravodeabril.movies.data.local.entity.MoviePicture
import pt.cravodeabril.movies.data.local.entity.MovieWithPictures
import pt.cravodeabril.movies.data.local.entity.Person
import pt.cravodeabril.movies.data.remote.MovieQueryResponse
import pt.cravodeabril.movies.data.remote.MovieServiceClient
import pt.cravodeabril.movies.data.remote.toEntity
import kotlin.time.Instant

class MovieRepository(private val movieDao: MovieDao) {
    fun getAllMoviesWithPictures(): Flow<List<MovieWithPictures>> =
        movieDao.getAllMoviesWithPictures()

    suspend fun getMovieWithDetailsById(id: Long): MovieDetailsManual {
        val movieWithPictures = movieDao.getMovieWithPictures(id)
        val allGenres = movieDao.getAllGenres()
        val allPersons = movieDao.getAllPersons()

        // Manual composition - simple and works!
        return MovieDetailsManual(
            movie = movieWithPictures?.movie ?: return MovieDetailsManual.empty(),
            pictures = movieWithPictures.pictures,
            genres = allGenres,
            persons = allPersons
        )
    }

//    fun getMoviesFlow(): Flow<List<Movie>> = flow {
//        emit(movieDao.getAllMovies())
//    }

    suspend fun upsertMovies(movies: List<Movie>) {
        movieDao.insertMovies(movies)
    }

    fun fetchMoviesFromApi(
        title: String? = null,
        genre: String? = null,
        sortBy: String = "releaseDate",
        sortOrder: String = "desc",
        favoritesOnly: Boolean = false,
        username: String = "admin",  // Replace with logged user credentials later
        password: String = "admin"
    ): Flow<ApiResult<List<MovieQueryResponse>>> {
        // Apply credentials & call Ktor client
        return MovieServiceClient.apply {
            setCredentials(username, password)
        }.getMovies(
            order = sortOrder,
            sortBy = sortBy,
            title = title,
            genre = genre,
            favoritesOnly = favoritesOnly
        )
    }


    fun refreshMoviesFromNetwork(
        title: String? = null,
        genre: String? = null,
        sortBy: String = "releaseDate",
        sortOrder: String = "desc",
        favoritesOnly: Boolean = false
    ): Flow<ApiResult<List<MovieQueryResponse>>> = flow {
        fetchMoviesFromApi(title, genre, sortBy, sortOrder, favoritesOnly).collect { result ->
            when (result) {
                is ApiResult.Success -> {
                    val entities = result.data.map { it.toEntity() }
                    upsertMovies(entities)
                    emit(ApiResult.Success(result.data))
                }

                is ApiResult.Failure -> emit(result)
                ApiResult.Loading -> emit(ApiResult.Loading)
            }
        }
    }

}

data class MovieDetailsManual(
    val movie: Movie,
    val pictures: List<MoviePicture>,
    val genres: List<Genre>,
    val persons: List<Person>
) {
    companion object {
        fun empty() = MovieDetailsManual(
            movie = Movie(0, "", "", 0, null, Instant.DISTANT_PAST, null),
            pictures = emptyList(),
            genres = emptyList(),
            persons = emptyList()
        )
    }
}