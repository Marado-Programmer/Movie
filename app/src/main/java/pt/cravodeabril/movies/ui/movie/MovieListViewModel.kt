@file:OptIn(ExperimentalUuidApi::class)

package pt.cravodeabril.movies.ui.movie

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateRange
import pt.cravodeabril.movies.data.ApiResult
import pt.cravodeabril.movies.data.ProblemDetails
import pt.cravodeabril.movies.data.local.AppDatabase
import pt.cravodeabril.movies.data.local.entity.MovieWithDetails
import pt.cravodeabril.movies.data.repository.MovieRepository
import kotlin.uuid.ExperimentalUuidApi

class MovieListViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase(app)
    private val repository = MovieRepository(db.movieDao(), db.genreDao(), db.personDao())

    // TODO: StateFlow
    private val _moviesFromApi = MutableLiveData<ApiResult<List<MovieWithDetails>>>()
    val movies: LiveData<ApiResult<List<MovieWithDetails>>> = _moviesFromApi

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
            repository.observeMovies(query, genres, date, rating, favorites, sortBy).collect { movies ->
                _moviesFromApi.postValue(ApiResult.Success(movies))
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            when (val result = repository.refreshMovies()) {
                is ApiResult.Failure -> _moviesFromApi.postValue(ApiResult.Failure(result.error))
                else -> Unit
            }
        }
    }

    fun toggleFavorite(movieId: Long) {
        viewModelScope.launch {
            repository.isFavorite(movieId)?.let { toggle -> repository.toggleFavorite(movieId, toggle) }
        }
    }
}
