@file:OptIn(ExperimentalUuidApi::class)

package pt.cravodeabril.movies.ui.genre

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.cravodeabril.movies.App
import pt.cravodeabril.movies.data.Resource
import pt.cravodeabril.movies.data.local.entity.GenreEntity
import kotlin.uuid.ExperimentalUuidApi

class GenreListViewModel(app: Application) : AndroidViewModel(app) {
    private val appContainer = (app as App).container

    private val repository = appContainer.movieRepository

    private val _genres = MutableLiveData<Resource<List<GenreEntity>>>()
    val genres: LiveData<Resource<List<GenreEntity>>> = _genres

    init {
        observeGenres()
        refresh()
    }

    fun observeGenres(query: String = "") {
        viewModelScope.launch {
            repository.observeGenres().collect { genres ->
                _genres.postValue(Resource.Success(genres.filter { genre ->
                    query == "" || genre.name.contains(query) || (genre.description?.contains(query)
                        ?: false)
                }))
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            when (val result = repository.refreshGenres()) {
                is Resource.Error -> _genres.postValue(result)
                else -> Unit
            }
        }
    }

    fun deleteGenre(id: Long) {
        viewModelScope.launch {
            repository.deleteGenre(id)
        }
    }
}
