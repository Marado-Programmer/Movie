package pt.cravodeabril.movies.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import pt.cravodeabril.movies.data.local.entity.FullPersonEntity
import pt.cravodeabril.movies.data.local.entity.GenreEntity
import pt.cravodeabril.movies.data.local.entity.PersonEntity
import pt.cravodeabril.movies.data.local.entity.PersonPictureEntity
import pt.cravodeabril.movies.data.local.entity.PictureEntity

@Dao
interface PersonDao {
    @Transaction
    @Query(
        """
    SELECT * FROM persons
    """
    )
    fun observePeople(): Flow<List<FullPersonEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPeople(people: List<PersonEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPerson(person: PersonEntity)

    @Query("DELETE FROM persons WHERE id = :id")
    suspend fun deletePerson(id: Long)

    @Transaction
    @Query(
        """
    SELECT * FROM person_pictures
    """
    )
    fun observePictures(): Flow<List<PersonPictureEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPictures(pictures: List<PersonPictureEntity>)

    @Query("DELETE FROM person_pictures WHERE id = :id")
    suspend fun deletePicture(id: Long)
}