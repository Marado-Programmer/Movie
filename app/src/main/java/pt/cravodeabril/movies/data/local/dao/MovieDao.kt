package pt.cravodeabril.movies.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import pt.cravodeabril.movies.data.local.entity.CastMemberEntity
import pt.cravodeabril.movies.data.local.entity.MovieEntity
import pt.cravodeabril.movies.data.local.entity.MovieGenreCrossRef
import pt.cravodeabril.movies.data.local.entity.MovieWithDetails
import pt.cravodeabril.movies.data.local.entity.PictureEntity
import pt.cravodeabril.movies.data.local.entity.UserFavoriteEntity
import pt.cravodeabril.movies.data.local.entity.UserRatingEntity

@Dao
interface MovieDao {
    @Transaction
    @Query(
        """
    SELECT * FROM movies
    ORDER BY
        CASE WHEN :sortBy = 'title' THEN title END COLLATE NOCASE,
        CASE WHEN :sortBy = 'rating' THEN rating END DESC,
        releaseDate DESC
    """
    )
    fun observeMovies(sortBy: String): Flow<List<MovieWithDetails>>

    @Transaction
    @Query(
        """
    SELECT * FROM movies
    JOIN user_favorites f ON f.movieId = movies.id
    WHERE f.userId = :userId
    ORDER BY
        CASE WHEN :sortBy = 'title' THEN title END COLLATE NOCASE,
        CASE WHEN :sortBy = 'rating' THEN rating END DESC,
        releaseDate DESC
    """
    )
    fun observeFavoriteMovies(userId: Long, sortBy: String): Flow<List<MovieWithDetails>>

    @Transaction
    @Query("SELECT * FROM movies WHERE id = :movieId")
    suspend fun observeMovie(movieId: Long): MovieWithDetails?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMovies(movies: List<MovieEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMovie(movie: MovieEntity)

    @Query("DELETE FROM movies WHERE id = :movieId")
    suspend fun deleteMovie(movieId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPictures(pictures: List<PictureEntity>)

    @Query("DELETE FROM pictures WHERE movieId = :movieId")
    suspend fun deletePicturesByMovie(movieId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGenres(genres: List<MovieGenreCrossRef>)

    @Query("DELETE FROM movie_genres WHERE movieId = :movieId")
    suspend fun clearMovieGenres(movieId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCast(cast: List<CastMemberEntity>)

    @Query("DELETE FROM cast_members WHERE movieId = :movieId")
    suspend fun deleteCastByMovie(movieId: Long)

    @Query(
        """
    SELECT EXISTS(
        SELECT 1 FROM user_favorites
        WHERE userId = :userId AND movieId = :movieId
    )
    """
    )
    suspend fun isFavorite(movieId: Long, userId: Long): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorites(favorites: List<UserFavoriteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: UserFavoriteEntity)

    @Query("DELETE FROM user_favorites WHERE userId = :userId AND movieId = :movieId")
    suspend fun removeFavorite(userId: Long, movieId: Long)

    @Query(
        """
    SELECT * FROM user_ratings
    WHERE userId = :userId AND movieId = :movieId
    """
    )
    suspend fun getRating(movieId: Long, userId: Long): UserRatingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRating(rating: UserRatingEntity)

    @Query("DELETE FROM user_ratings WHERE userId = :userId AND movieId = :movieId")
    suspend fun deleteRating(userId: Long, movieId: Long)
}