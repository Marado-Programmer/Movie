package pt.cravodeabril.movies.ui.movie

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.cravodeabril.movies.data.ApiResult
import pt.cravodeabril.movies.data.ProblemDetails
import pt.cravodeabril.movies.data.local.AppDatabase
import pt.cravodeabril.movies.data.local.entity.MovieWithDetails
import pt.cravodeabril.movies.data.repository.MovieRepository

class MovieDetailsViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase(app)
    private val repository = MovieRepository(db.movieDao(), db.genreDao(), db.personDao())

    private val _movie = MutableLiveData<ApiResult<MovieWithDetails>>()
    val movie: LiveData<ApiResult<MovieWithDetails>> = _movie

    fun observeMovie(id: Long) {
        viewModelScope.launch {
            val movie = repository.observeMovie(id)
            if (movie != null) {
                _movie.postValue(ApiResult.Success(movie))
            } else {
                _movie.postValue(ApiResult.Failure(ProblemDetails("404", "", 404, "")))
            }
        }
    }

    fun refresh(id: Long) {
        viewModelScope.launch {
            when (val result = repository.refreshMovie(id)) {
                is ApiResult.Failure -> _movie.postValue(ApiResult.Failure(result.error))
                else -> Unit
            }
        }
    }

    fun toggleFavorite() {
        val result = movie.value
        if (result is ApiResult.Success) {
            val id = result.data.movie.id
            viewModelScope.launch {
                repository.isFavorite(id)?.let { toggle -> repository.toggleFavorite(id, toggle) }
            }
        }
    }
}
