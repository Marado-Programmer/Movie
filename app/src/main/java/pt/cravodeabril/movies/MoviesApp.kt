package pt.cravodeabril.movies

import android.app.Application
import coil3.SingletonImageLoader
import pt.cravodeabril.movies.data.createCoilImageLoader

class MoviesApp : Application() {

    override fun onCreate() {
        super.onCreate()
        SingletonImageLoader.setSafe {
            createCoilImageLoader(it)
        }

    }
}