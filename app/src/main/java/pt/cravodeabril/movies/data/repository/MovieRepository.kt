package pt.cravodeabril.movies.data.repository

import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import pt.cravodeabril.movies.data.ProblemDetails
import pt.cravodeabril.movies.data.Resource
import pt.cravodeabril.movies.data.local.dao.GenreDao
import pt.cravodeabril.movies.data.local.dao.MovieDao
import pt.cravodeabril.movies.data.local.dao.PersonDao
import pt.cravodeabril.movies.data.local.entity.CastMemberEntity
import pt.cravodeabril.movies.data.local.entity.FullPersonEntity
import pt.cravodeabril.movies.data.local.entity.GenreEntity
import pt.cravodeabril.movies.data.local.entity.MovieEntity
import pt.cravodeabril.movies.data.local.entity.MovieGenreCrossRef
import pt.cravodeabril.movies.data.local.entity.MovieWithDetails
import pt.cravodeabril.movies.data.local.entity.PersonEntity
import pt.cravodeabril.movies.data.local.entity.PersonPictureEntity
import pt.cravodeabril.movies.data.local.entity.PictureEntity
import pt.cravodeabril.movies.data.local.entity.UserFavoriteEntity
import pt.cravodeabril.movies.data.local.entity.UserRatingEntity
import pt.cravodeabril.movies.data.remote.AddPicturesToPersonCommand
import pt.cravodeabril.movies.data.remote.CreateGenre
import pt.cravodeabril.movies.data.remote.CreateGenresCommand
import pt.cravodeabril.movies.data.remote.CreateMovieCommand
import pt.cravodeabril.movies.data.remote.CreatePersonCommand
import pt.cravodeabril.movies.data.remote.CreatePicture
import pt.cravodeabril.movies.data.remote.Genre
import pt.cravodeabril.movies.data.remote.MovieDetail
import pt.cravodeabril.movies.data.remote.MovieServiceClient
import pt.cravodeabril.movies.data.remote.MovieSimple
import pt.cravodeabril.movies.data.remote.Person
import pt.cravodeabril.movies.data.remote.RemovePicturesFromPersonCommand
import pt.cravodeabril.movies.data.remote.RoleAssignment
import pt.cravodeabril.movies.data.remote.UpdateGenreCommand
import pt.cravodeabril.movies.data.remote.UpdateMovieCommand
import pt.cravodeabril.movies.data.remote.UpdatePersonCommand
import kotlin.math.roundToInt

class MovieRepository(
    private val movieDao: MovieDao,
    private val genreDao: GenreDao,
    private val personDao: PersonDao,
    private val login: LoginRepository,
) {
    // TODO: filter by `director`
    fun observeMovies(
        query: String = "",
        genres: List<String> = emptyList(),
        date: LocalDateRange? = null,
        rating: IntRange? = null,
        favorites: Long? = null,
        sortBy: String = "releaseDate"
    ): Flow<List<MovieWithDetails>> {
        val movies =
            if (favorites == null) movieDao.observeMovies(sortBy) else movieDao.observeFavoriteMovies(
                favorites, sortBy
            )
        return movies.map { movies ->
            movies.filter {
                it.movie.title.contains(
                    query, true
                ) || it.movie.synopsis.contains(query, true)
            }.filter { genres.isEmpty() || it.genres.any { g -> g.name in genres } }
                .filter { date == null || date.contains(it.movie.releaseDate) }
                .filter { rating == null || (it.movie.rating != null && rating.contains(it.movie.rating.roundToInt())) }
        }
    }

    suspend fun refreshMovies(
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
    ): Resource<Unit> {
        val result = MovieServiceClient.getMovies(
            offset,
            count,
            director,
            genre,
            title,
            fromReleaseDate,
            toReleaseDate,
            fromRating,
            toRating,
            favoritesOnly,
            sortBy,
            sortOrder
        )
        return when (result) {
            is Resource.Success -> {
                persistMovies(result.data)
                Resource.Success(Unit)
            }

            is Resource.Error -> result
            else -> Resource.Error(
                problem = ProblemDetails("Unknown", "Unexpected state", 500, "")
            )
        }
    }

    suspend fun observeMovie(id: Long): MovieWithDetails? = movieDao.observeMovie(id)

    suspend fun refreshMovie(movieId: Long): Resource<Unit> {
        return when (val result =
            MovieServiceClient.getMovie(movieId).first { it !is Resource.Loading }) {
            is Resource.Success -> {
                persistMovie(result.data)
                Resource.Success(Unit)
            }

            is Resource.Error -> result
            else -> Resource.Error(
                problem = ProblemDetails("Unknown", "Unexpected state", 500, "")
            )
        }
    }

    @Transaction
    private suspend fun persistMovies(apiMovies: List<MovieSimple>) {
        val userId = login.user?.id

        val movies = apiMovies.map {
            MovieEntity(
                id = it.id.toLong(),
                title = it.title,
                synopsis = it.synopsis,
                releaseDate = it.releaseDate,
                directorId = it.director?.personId?.toLong(),
                rating = it.rating,
                minimumAge = null,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt
            )
        }

        movieDao.upsertMovies(movies)

//        genreDao.upsertGenres(
//            apiMovies.flatMap { it.genres }.distinct()
//            .map { GenreEntity(null, it, description = null, averageRating = 0f) })
//
//        movieDao.upsertGenres(
//            apiMovies.flatMap { movie ->
//                movie.genres.map { MovieGenreCrossRef(movie.id.toLong(), it) }
//            })

//        genreDao.upsertGenres(
//            apiMovies.flatMap { it.genres }.distinct()
//                .map {
//                    GenreEntity(
//                        name = it,
//                        description = null,
//                        averageRating = 0f,
//                    )
//                }
//        )
//
//        movieDao.upsertGenres(
//            apiMovies.flatMap { movie ->
//                movie.genres.map {
//                    MovieGenreCrossRef(
//                        movieId = movie.id.toLong(),
//                        genreId = it.id.toLong()
//                    )
//                }
//            }
//        )


        movieDao.upsertPictures(
            apiMovies.mapNotNull { movie ->
                movie.mainPicture?.let {
                    PictureEntity(
                        id = it.id.toLong(),
                        movieId = movie.id.toLong(),
                        filename = it.filename,
                        contentType = it.contentType,
                        mainPicture = it.mainPicture,
                        description = it.description
                    )
                }
            })

        if (userId != null) {
            movieDao.addFavorites(apiMovies.filter { it.favorite }
                .map { UserFavoriteEntity(userId, it.id.toLong()) })
        }

        personDao.upsertPeople(apiMovies.mapNotNull { it.director }.distinctBy { it.personId }
            .map { PersonEntity(it.personId.toLong(), it.name, dateOfBirth = null) })
    }

    @Transaction
    private suspend fun persistMovie(apiMovie: MovieDetail) {
        val userId = login.user?.id

        val movie = MovieEntity(
            id = apiMovie.id.toLong(),
            title = apiMovie.title,
            synopsis = apiMovie.synopsis,
            releaseDate = apiMovie.releaseDate,
            directorId = apiMovie.director?.personId?.toLong(),
            rating = apiMovie.rating?.average,
            minimumAge = apiMovie.minimumAge,
            createdAt = apiMovie.createdAt,
            updatedAt = apiMovie.updatedAt
        )

        movieDao.upsertMovie(movie)

        genreDao.upsertGenres(apiMovie.genres.distinctBy { it.id }.map {
            GenreEntity(
                id = it.id.toLong(),
                name = it.name,
                description = it.description,
                averageRating = 0f
            )
        })

        movieDao.upsertGenres(apiMovie.genres.map {
            MovieGenreCrossRef(
                movieId = apiMovie.id.toLong(), genreId = it.id.toLong()
            )
        })

        val director = apiMovie.director?.let {
            PersonEntity(
                it.personId.toLong(), it.name, dateOfBirth = null
            )
        }
        if (director != null) {
            personDao.upsertPerson(director)
        }
        personDao.upsertPeople(
            apiMovie.cast.distinct().map {
                PersonEntity(
                    id = it.personId.toLong(), name = it.name, dateOfBirth = null
                )
            })


        movieDao.upsertCast(apiMovie.cast.map { cast ->
            CastMemberEntity(apiMovie.id.toLong(), cast.personId.toLong(), cast.character)
        })

        movieDao.upsertPictures(apiMovie.pictures.distinct().map {
            PictureEntity(
                it.id.toLong(),
                apiMovie.id.toLong(),
                it.filename,
                it.contentType,
                it.mainPicture,
                it.description
            )
        })

        if (userId != null && apiMovie.favorite) {
            movieDao.addFavorite(UserFavoriteEntity(0, apiMovie.id.toLong()))
        }

        if (userId != null && apiMovie.userRating != null) {
            movieDao.upsertRating(
                UserRatingEntity(
                    0, apiMovie.id.toLong(), apiMovie.userRating.rating, apiMovie.userRating.comment
                )
            )
        }
    }

    suspend fun setFavorite(
        movieId: Long, favorite: Boolean
    ): Resource<Unit> {
        val userId = login.user?.id ?: return Resource.Error(
            problem = ProblemDetails("Auth", "Not logged in", 401, "")
        )

        val result = MovieServiceClient.markAsFavorite(movieId, favorite)

        if (result is Resource.Success) {
            if (favorite) {
                movieDao.addFavorite(UserFavoriteEntity(userId, movieId))
            } else {
                movieDao.removeFavorite(userId, movieId)
            }
        }

        return result
    }


    suspend fun isFavorite(movieId: Long): Boolean? {
        val userId = login.user?.id

        if (userId != null) {
            return movieDao.isFavorite(movieId, userId)
        }

        return null
    }

    suspend fun createMovie(
        title: String,
        synopsis: String,
        releaseDate: LocalDate,
        minimumAge: Int,
        genres: Set<Int>,
        directorId: Long?,
        cast: List<RoleAssignment> = emptyList(),
        pictures: List<CreatePicture> = emptyList()
    ): Resource<MovieDetail> {

        val command = CreateMovieCommand(
            title = title,
            synopsis = synopsis,
            cast = cast,
            pictures = pictures,
            genres = genres,
            directorId = directorId?.toInt(),
            releaseDate = releaseDate,
            minimumAge = minimumAge
        )

        val result = MovieServiceClient.createMovie(command)

        return when (result) {
            is Resource.Success -> {
                persistMovie(result.data)
                Resource.Success(result.data)
            }

            is Resource.Error -> result

            else -> Resource.Error(
                problem = ProblemDetails("Unknown", "Unexpected state", 500, "")
            )
        }
    }

    suspend fun updateMovie(
        id: Long,
        title: String,
        synopsis: String,
        releaseDate: LocalDate,
        minimumAge: Int,
        genres: Set<Int>,
        directorId: Long?
    ): Resource<MovieDetail> {

        val command = UpdateMovieCommand(
            id = id.toInt(),
            title = title,
            synopsis = synopsis,
            genres = genres,
            directorId = directorId?.toInt(),
            releaseDate = releaseDate,
            minimumAge = minimumAge
        )

        return when (val result = MovieServiceClient.updateMovie(command)) {
            is Resource.Success -> {
                persistMovie(result.data)
                Resource.Success(result.data)
            }

            is Resource.Error -> result

            else -> Resource.Error(
                problem = ProblemDetails("Unknown", "Unexpected state", 500, "")
            )
        }
    }

    suspend fun deleteMovie(movieId: Long): Resource<Unit> {
        return when (val result = MovieServiceClient.deleteMovie(movieId)) {
            is Resource.Success -> {
                movieDao.deleteCastByMovie(movieId)
                movieDao.deletePicturesByMovie(movieId)
                movieDao.clearMovieGenres(movieId)
                movieDao.deleteMovie(movieId)
                Resource.Success(Unit)
            }

            is Resource.Error -> result

            else -> Resource.Error(
                problem = ProblemDetails("Unknown", "Unexpected state", 500, "")
            )
        }
    }

    fun observeGenres(assignedOnly: Boolean? = null): Flow<List<GenreEntity>> {
        return if (assignedOnly == true) genreDao.observeGenres()
        else genreDao.observeGenres()
    }

    suspend fun refreshGenres(assignedOnly: Boolean? = false): Resource<Unit> {
        val result = MovieServiceClient.getGenres(assignedOnly)
        return when (result) {
            is Resource.Success -> {
                persistGenres(result.data)
                Resource.Success(Unit)
            }

            is Resource.Error -> result
            else -> Resource.Error(
                problem = ProblemDetails("Unknown", "Unexpected state", 500, "")
            )
        }
    }

    @Transaction
    private suspend fun persistGenres(genres: List<Genre>) {
        val userId = login.user?.id

        val genres = genres.map {
            GenreEntity(
                id = it.id.toLong(),
                name = it.name,
                description = it.description,
                averageRating = 0f
            )
        }

        genreDao.upsertGenres(genres)
    }

    suspend fun deleteGenre(id: Long): Resource<Unit> {
        return when (val result = MovieServiceClient.deleteGenre(id)) {
            is Resource.Success -> {
                genreDao.deleteGenre(id)
                Resource.Success(Unit)
            }

            is Resource.Error -> result

            else -> Resource.Error(
                problem = ProblemDetails("Unknown", "Unexpected state", 500, "")
            )
        }
    }

    suspend fun createGenre(
        name: String,
        description: String?,
    ): Resource<List<Genre>> {
        val command = CreateGenresCommand(
            listOf(
                CreateGenre(name = name, description = description)
            )
        )

        val result = MovieServiceClient.createGenres(command)

        return when (result) {
            is Resource.Success -> {
                persistGenres(result.data)
                Resource.Success(result.data)
            }

            is Resource.Error -> result

            else -> Resource.Error(
                problem = ProblemDetails("Unknown", "Unexpected state", 500, "")
            )
        }
    }

    suspend fun updateGenre(
        id: Long,
        name: String,
        description: String?,
    ): Resource<Genre> {

        val command = UpdateGenreCommand(id = id.toInt(), name = name, description = description)

        return when (val result = MovieServiceClient.updateGenre(command)) {
            is Resource.Success -> {
                persistGenres(listOf(result.data))
                Resource.Success(result.data)
            }

            is Resource.Error -> result

            else -> Resource.Error(
                problem = ProblemDetails("Unknown", "Unexpected state", 500, "")
            )
        }
    }

    fun observePeople(): Flow<List<FullPersonEntity>> {
        return personDao.observePeople()
    }

    suspend fun refreshPeople(): Resource<Unit> {
        val result = MovieServiceClient.getPeople()
        return when (result) {
            is Resource.Success -> {
                persistPeople(result.data.map {
                    Person(
                        id = it.id,
                        name = it.name,
                        dateOfBirth = it.dateOfBirth,
                        pictures = it.picture?.let { pic -> listOf(pic) } ?: listOf())
                })
                Resource.Success(Unit)
            }

            is Resource.Error -> result
            else -> Resource.Error(
                problem = ProblemDetails("Unknown", "Unexpected state", 500, "")
            )
        }
    }

    @Transaction
    private suspend fun persistPeople(people: List<Person>) {
        val userId = login.user?.id

        personDao.upsertPeople(people.map {
            PersonEntity(
                id = it.id.toLong(), name = it.name, dateOfBirth = it.dateOfBirth
            )
        })

        personDao.upsertPictures(people.flatMap {
            it.pictures.map { pic ->
                PersonPictureEntity(
                    id = pic.id.toLong(),
                    personId = it.id.toLong(),
                    filename = pic.filename,
                    contentType = pic.contentType,
                    mainPicture = pic.mainPicture,
                    description = pic.description,
                )
            }
        })
    }

    suspend fun deletePerson(id: Long): Resource<Unit> {
        return when (val result = MovieServiceClient.deletePerson(id)) {
            is Resource.Success -> {
                personDao.deletePerson(id)
                Resource.Success(Unit)
            }

            is Resource.Error -> result

            else -> Resource.Error(
                problem = ProblemDetails("Unknown", "Unexpected state", 500, "")
            )
        }
    }

    suspend fun createPerson(
        name: String, dateOfBirth: LocalDate?, pictures: List<CreatePicture>
    ): Resource<Person> {
        val command = CreatePersonCommand(name, dateOfBirth, pictures)

        val result = MovieServiceClient.createPerson(command)

        return when (result) {
            is Resource.Success -> {
                persistPeople(listOf(result.data))
                Resource.Success(result.data)
            }

            is Resource.Error -> result

            else -> Resource.Error(
                problem = ProblemDetails("Unknown", "Unexpected state", 500, "")
            )
        }
    }

    suspend fun updatePerson(
        id: Long, name: String, dateOfBirth: LocalDate?
    ): Resource<Person> {

        val command = UpdatePersonCommand(id = id.toInt(), name = name, dateOfBirth = dateOfBirth)

        return when (val result = MovieServiceClient.updatePerson(command)) {
            is Resource.Success -> {
                persistPeople(listOf(result.data))
                Resource.Success(result.data)
            }

            is Resource.Error -> result

            else -> Resource.Error(
                problem = ProblemDetails("Unknown", "Unexpected state", 500, "")
            )
        }
    }

    @Transaction
    suspend fun updatePersonPicture(
        id: Long, picture: CreatePicture
    ): Resource<Person> {
        val command = AddPicturesToPersonCommand(id.toInt(), listOf(picture))

        return when (val result = MovieServiceClient.createPersonPictures(command)) {
            is Resource.Success -> {
                persistPeople(listOf(result.data))

                val command = RemovePicturesFromPersonCommand(
                    id.toInt(),
                    personDao.observePictures().first().map {
                        it.id.toInt()
                    }.toSet()
                )

                return when (val result = MovieServiceClient.removePersonPictures(command)) {
                    is Resource.Success -> {
                        command.pictures.forEach { personDao.deletePicture(it.toLong()) }
                        Resource.Success(result.data)
                    }

                    is Resource.Error -> result

                    else -> Resource.Error(
                        problem = ProblemDetails("Unknown", "Unexpected state", 500, "")
                    )
                }
            }

            is Resource.Error -> result

            else -> Resource.Error(
                problem = ProblemDetails("Unknown", "Unexpected state", 500, "")
            )
        }
    }
}