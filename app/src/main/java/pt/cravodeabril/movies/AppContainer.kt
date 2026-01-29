package pt.cravodeabril.movies

import android.content.Context
import pt.cravodeabril.movies.data.local.AppDatabase
import pt.cravodeabril.movies.data.remote.LoginDataSource
import pt.cravodeabril.movies.data.repository.LoginRepository
import pt.cravodeabril.movies.data.repository.MovieRepository

/**
 * Container to allow the app to share some resources (e.g. LoginRepository)
 */
class AppContainer(context: Context) {
    val database = AppDatabase(context)

    val loginRepository by lazy {
        LoginRepository(
            dataSource = LoginDataSource(),
            userDao = database.userDao()
        )
    }

    val movieRepository by lazy {
        MovieRepository(
            database.movieDao(),
            database.genreDao(),
            database.personDao(),
            loginRepository
        )
    }
}
