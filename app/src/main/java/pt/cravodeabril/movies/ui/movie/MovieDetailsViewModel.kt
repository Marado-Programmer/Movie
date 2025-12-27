package pt.cravodeabril.movies.ui.movie

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.cravodeabril.movies.data.local.AppDatabase
import pt.cravodeabril.movies.data.repository.MovieDetailsManual
import pt.cravodeabril.movies.data.repository.MovieRepository

class MovieDetailsViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase(app)
    private val repository = MovieRepository(db.movieDao())

    private val _movie = MutableLiveData<MovieDetailsManual?>()
    val movie: LiveData<MovieDetailsManual?> = _movie

    fun loadMovieById(movieId: Long) {
        viewModelScope.launch {
            _movie.postValue(repository.getMovieWithDetailsById(movieId))
        }
    }

    fun toggleFavorite(movieId: Long) {
        viewModelScope.launch {
            // TODO: call MovieServiceClient.put("/movies/{id}/mark-as-favorite")
        }
    }
}
