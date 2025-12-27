package pt.cravodeabril.movies.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import pt.cravodeabril.movies.data.local.entity.Genre
import pt.cravodeabril.movies.data.local.entity.Movie
import pt.cravodeabril.movies.data.local.entity.MovieWithPictures
import pt.cravodeabril.movies.data.local.entity.Person

@Dao
interface MovieDao {
    @Transaction
    @Query("SELECT * FROM movies")
    fun getAllMoviesWithPictures(): Flow<List<MovieWithPictures>>

    @Transaction
    @Query("SELECT * FROM movies WHERE id = :movieId")
    suspend fun getMovieWithPictures(movieId: Long): MovieWithPictures?

    @Query("SELECT * FROM genres")
    suspend fun getAllGenres(): List<Genre>

    @Query("SELECT * FROM persons")
    suspend fun getAllPersons(): List<Person>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<Movie>)
}
