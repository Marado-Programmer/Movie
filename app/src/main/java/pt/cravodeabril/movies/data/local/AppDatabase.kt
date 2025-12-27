package pt.cravodeabril.movies.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import pt.cravodeabril.movies.data.local.dao.MovieDao
import pt.cravodeabril.movies.data.local.entity.CastMember
import pt.cravodeabril.movies.data.local.entity.Genre
import pt.cravodeabril.movies.data.local.entity.Movie
import pt.cravodeabril.movies.data.local.entity.MovieGenre
import pt.cravodeabril.movies.data.local.entity.MoviePicture
import pt.cravodeabril.movies.data.local.entity.Person
import pt.cravodeabril.movies.utils.InstantConverter

@Database(
    entities = [
        Movie::class,
        MoviePicture::class,
        Genre::class,
        MovieGenre::class,
        Person::class,
        CastMember::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(InstantConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao

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
