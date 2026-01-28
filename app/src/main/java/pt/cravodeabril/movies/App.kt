package pt.cravodeabril.movies

import android.app.Application
import coil3.SingletonImageLoader
import pt.cravodeabril.movies.data.createCoilImageLoader

class App : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        SingletonImageLoader.setSafe {
            createCoilImageLoader(it, container.loginRepository)
        }
    }
}