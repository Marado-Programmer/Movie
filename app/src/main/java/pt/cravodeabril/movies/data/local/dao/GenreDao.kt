package pt.cravodeabril.movies.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import pt.cravodeabril.movies.data.local.entity.GenreEntity

@Dao
interface GenreDao {
    @Transaction
    @Query(
        """
    SELECT * FROM genres
    """
    )
    fun observeGenres(): Flow<List<GenreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGenres(genres: List<GenreEntity>)

    @Query("DELETE FROM genres WHERE id = :id")
    suspend fun deleteGenre(id: Long)
}