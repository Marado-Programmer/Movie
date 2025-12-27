package pt.cravodeabril.movies

import androidx.core.content.edit
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import pt.cravodeabril.movies.data.TodoServiceClient

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("pt.cravodeabril.movies", appContext.packageName)



        appContext.getSharedPreferences("prefs", 0).edit {
            putString("username", "user1")
            putString("password", "user1")
        }


        val todoClient = TodoServiceClient
        runBlocking {

            todoClient.getTodos()


        }

    }
}