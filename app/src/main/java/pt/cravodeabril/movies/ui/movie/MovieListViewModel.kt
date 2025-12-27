@file:OptIn(ExperimentalUuidApi::class)

package pt.cravodeabril.movies.ui.movie

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.cravodeabril.movies.data.ApiResult
import pt.cravodeabril.movies.data.local.AppDatabase
import pt.cravodeabril.movies.data.local.entity.Movie
import pt.cravodeabril.movies.data.local.entity.MovieWithPictures
import pt.cravodeabril.movies.data.remote.toEntity
import pt.cravodeabril.movies.data.repository.MovieRepository
import kotlin.uuid.ExperimentalUuidApi

class MovieListViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase(app)
    private val repository = MovieRepository(db.movieDao())

    private val _moviesFromApi = MutableLiveData<ApiResult<List<Movie>>>()
    val movies: LiveData<ApiResult<List<Movie>>> = _moviesFromApi
    private val _moviesWithPictures = MutableLiveData<List<MovieWithPictures>>(emptyList())
    val moviesWithPictures: LiveData<List<MovieWithPictures>> = _moviesWithPictures

    init {
        loadMoviesWithPictures()
    }

    fun loadMoviesWithPictures() {
        viewModelScope.launch {
            try {
                repository.getAllMoviesWithPictures()
                    .collect { data -> _moviesWithPictures.postValue(data) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadMoviesOnline(
        title: String? = null,
        genre: String? = null,
        sortBy: String = "releaseDate",
        sortOrder: String = "desc",
        favoritesOnly: Boolean = false
    ) {
        viewModelScope.launch {
            repository.refreshMoviesFromNetwork(
                title = title,
                genre = genre,
                sortBy = sortBy,
                sortOrder = sortOrder,
                favoritesOnly = favoritesOnly
            ).collect { result ->
                _moviesFromApi.value = when (result) {
                    is ApiResult.Success -> {
                        val entities = result.data.map { it.toEntity() }
                        _moviesFromApi.postValue(ApiResult.Success(entities))
                        ApiResult.Success(entities)
                    }

                    is ApiResult.Failure -> result
                    ApiResult.Loading -> ApiResult.Loading
                }
            }
        }
    }
}
