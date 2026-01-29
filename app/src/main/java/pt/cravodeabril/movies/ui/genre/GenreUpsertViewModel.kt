package pt.cravodeabril.movies.ui.genre

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.ktor.util.encodeBase64
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import pt.cravodeabril.movies.App
import pt.cravodeabril.movies.data.ProblemDetails
import pt.cravodeabril.movies.data.Resource
import pt.cravodeabril.movies.data.remote.CreatePicture
import pt.cravodeabril.movies.utils.FormState

class GenreUpsertViewModel(app: Application, private val id: Long? = null) : AndroidViewModel(app) {
    private val appContainer = getApplication<App>().container

    private val repository = appContainer.movieRepository

    val isEditMode: Boolean = id != null

    val name = MutableLiveData("")
    val description = MutableLiveData("")

    private val _state = MutableLiveData<FormState>(FormState.Idle)
    val state: LiveData<FormState> = _state

    init {
        if (isEditMode) load()
    }

    private fun load() {
        viewModelScope.launch {
            _state.value = FormState.Loading

            val refreshResult = repository.refreshGenres()
            if (refreshResult is Resource.Error) {
                _state.value = FormState.Error(refreshResult.problem)
                return@launch
            }

            val genre =
                repository.observeGenres().firstOrNull()?.firstOrNull { genre -> genre.id == id }

            if (genre == null) {
                _state.value = FormState.Error(
                    ProblemDetails("404", "Genre not found", 404, "")
                )
                return@launch
            }

            name.value = genre.name
            description.value = genre.description

            _state.value = FormState.Idle
        }
    }

    fun save() {
        if (!validate()) return

        viewModelScope.launch {
            _state.value = FormState.Loading

            val result = if (isEditMode) {
                repository.updateGenre(
                    id = id!!,
                    name = name.value!!,
                    description = description.value
                )
            } else {
                repository.createGenre(
                    name = name.value!!,
                    description = description.value
                )
            }

            when (result) {
                is Resource.Success -> _state.value = FormState.Saved
                is Resource.Error -> {
                    _state.value = FormState.Error(result.problem)
                }

                else -> {}
            }
        }
    }

    fun delete() {
        if (!isEditMode) return

        viewModelScope.launch {
            _state.value = FormState.Loading

            when (val result = repository.deleteGenre(id!!)) {
                is Resource.Success -> _state.value = FormState.Deleted
                is Resource.Error -> _state.value = FormState.Error(result.problem)
                else -> {}
            }
        }
    }

    private fun validate(): Boolean {
        if (name.value.isNullOrBlank()) {
            _state.value = FormState.Error(ProblemDetails("400", "Title is required", 400, ""))
            return false
        }

        return true
    }
}