package pt.cravodeabril.movies.ui.movie

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.cravodeabril.movies.App
import pt.cravodeabril.movies.data.ProblemDetails
import pt.cravodeabril.movies.data.Resource
import pt.cravodeabril.movies.data.local.entity.MovieWithDetails

class MovieDetailsViewModel(app: Application) : AndroidViewModel(app) {
    private val appContainer = (app as App).container

    private val repository = appContainer.movieRepository

    private val _movie = MutableLiveData<Resource<MovieWithDetails>>()
    val movie: LiveData<Resource<MovieWithDetails>> = _movie

    fun observeMovie(id: Long) {
        viewModelScope.launch {
            val movie = repository.observeMovie(id)
            if (movie != null) {
                _movie.postValue(Resource.Success(movie))
            } else {
                _movie.postValue(Resource.Error(problem = ProblemDetails("404", "", 404, "")))
            }
        }
    }

    fun refresh(id: Long) {
        viewModelScope.launch {
            when (val result = repository.refreshMovie(id)) {
                is Resource.Error -> _movie.postValue(result)
                else -> Unit
            }
        }
    }

    fun toggleFavorite() {
        val result = movie.value
        if (result is Resource.Success) {
            val id = result.data.movie.id
            viewModelScope.launch {
                repository.isFavorite(id)?.let { toggle -> repository.toggleFavorite(id, toggle) }
            }
        }
    }
}
