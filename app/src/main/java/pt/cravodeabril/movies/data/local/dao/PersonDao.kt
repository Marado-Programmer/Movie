package pt.cravodeabril.movies.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import pt.cravodeabril.movies.data.local.entity.GenreEntity
import pt.cravodeabril.movies.data.local.entity.MovieEntity
import pt.cravodeabril.movies.data.local.entity.MovieGenreCrossRef
import pt.cravodeabril.movies.data.local.entity.MovieWithDetails
import pt.cravodeabril.movies.data.local.entity.PersonEntity
import pt.cravodeabril.movies.data.local.entity.PictureEntity
import pt.cravodeabril.movies.data.local.entity.UserFavoriteEntity
import pt.cravodeabril.movies.data.local.entity.UserRatingEntity

@Dao
interface PersonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPersons(genres: List<PersonEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPerson(genre: PersonEntity)
}