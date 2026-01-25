@file:OptIn(ExperimentalUuidApi::class)

package pt.cravodeabril.movies.ui.movie

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateRange
import pt.cravodeabril.movies.App
import pt.cravodeabril.movies.data.Resource
import pt.cravodeabril.movies.data.local.entity.MovieWithDetails
import kotlin.uuid.ExperimentalUuidApi

class MovieListViewModel(app: App) : AndroidViewModel(app) {
    private val appContainer = app.container

    private val repository = appContainer.movieRepository

    // TODO: StateFlow
    private val _moviesFromApi = MutableLiveData<Resource<List<MovieWithDetails>>>()
    val movies: LiveData<Resource<List<MovieWithDetails>>> = _moviesFromApi

    init {
        observeMovies()
        refresh()
    }

    fun observeMovies(
        query: String = "",
        genres: List<String> = emptyList(),
        date: LocalDateRange? = null,
        rating: IntRange? = null,
        favorites: Long? = null,
        sortBy: String = "releaseDate"
    ) {
        viewModelScope.launch {
            repository.observeMovies(query, genres, date, rating, favorites, sortBy)
                .collect { movies ->
                    _moviesFromApi.postValue(Resource.Success(movies))
                }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            when (val result = repository.refreshMovies()) {
                is Resource.Error -> _moviesFromApi.postValue(result)
                else -> Unit
            }
        }
    }

    fun toggleFavorite(movieId: Long) {
        viewModelScope.launch {
            repository.isFavorite(movieId)
                ?.let { toggle -> repository.toggleFavorite(movieId, toggle) }
        }
    }
}
