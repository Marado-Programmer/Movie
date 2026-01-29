package pt.cravodeabril.movies.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import pt.cravodeabril.movies.data.local.dao.GenreDao
import pt.cravodeabril.movies.data.local.dao.MovieDao
import pt.cravodeabril.movies.data.local.dao.PersonDao
import pt.cravodeabril.movies.data.local.dao.UserDao
import pt.cravodeabril.movies.data.local.entity.CastMemberEntity
import pt.cravodeabril.movies.data.local.entity.GenreEntity
import pt.cravodeabril.movies.data.local.entity.MovieEntity
import pt.cravodeabril.movies.data.local.entity.MovieGenreCrossRef
import pt.cravodeabril.movies.data.local.entity.PersonEntity
import pt.cravodeabril.movies.data.local.entity.PersonPictureEntity
import pt.cravodeabril.movies.data.local.entity.PictureEntity
import pt.cravodeabril.movies.data.local.entity.UserEntity
import pt.cravodeabril.movies.data.local.entity.UserFavoriteEntity
import pt.cravodeabril.movies.data.local.entity.UserRatingEntity
import pt.cravodeabril.movies.utils.InstantConverter
import pt.cravodeabril.movies.utils.LocalDateConverter

@Database(
    entities = [MovieEntity::class, PictureEntity::class, MovieGenreCrossRef::class, CastMemberEntity::class, UserRatingEntity::class, UserFavoriteEntity::class, GenreEntity::class, PersonEntity::class, UserEntity::class, PersonPictureEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(InstantConverter::class, LocalDateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
    abstract fun genreDao(): GenreDao
    abstract fun personDao(): PersonDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context.applicationContext).also { instance = it }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "todos.db")
                // .allowMainThreadQueries()
                .build()
    }

}
