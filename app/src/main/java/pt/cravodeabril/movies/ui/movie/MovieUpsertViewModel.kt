package pt.cravodeabril.movies.ui.movie

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.ktor.util.encodeBase64
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import pt.cravodeabril.movies.App
import pt.cravodeabril.movies.data.ProblemDetails
import pt.cravodeabril.movies.data.Resource
import pt.cravodeabril.movies.data.remote.CreatePicture

class MovieUpsertViewModel(app: Application, private val movieId: Long? = null) :
    AndroidViewModel(app) {
    private val appContainer = getApplication<App>().container

    private val repository = appContainer.movieRepository

    val isEditMode: Boolean = movieId != null

    /* ---------- Form fields ---------- */

    val title = MutableLiveData("")
    val synopsis = MutableLiveData("")
    val picture = MutableLiveData<Uri>()
    val releaseDate = MutableLiveData<LocalDate>()
    val minimumAge = MutableLiveData(0)
    val directorId = MutableLiveData<Long?>()
    val selectedGenres = MutableLiveData<Set<Long>>(emptySet())

    /* ---------- UI state ---------- */

    private val _state = MutableLiveData<MovieFormState>(MovieFormState.Idle)
    val state: LiveData<MovieFormState> = _state

    init {
        if (isEditMode) loadMovie()
    }

    /* ---------- Load existing movie ---------- */

    private fun loadMovie() {
        viewModelScope.launch {
            _state.value = MovieFormState.Loading

            val refreshResult = repository.refreshMovie(movieId!!)
            if (refreshResult is Resource.Error) {
                _state.value = MovieFormState.Error(refreshResult.problem)
                return@launch
            }

            val movie = repository.observeMovie(movieId)

            if (movie == null) {
                _state.value = MovieFormState.Error(
                    ProblemDetails("404", "Movie not found", 404, "")
                )
                return@launch
            }

            title.value = movie.movie.title
            synopsis.value = movie.movie.synopsis
            releaseDate.value = movie.movie.releaseDate
            minimumAge.value = movie.movie.minimumAge ?: 0
            directorId.value = movie.movie.directorId
            selectedGenres.value = movie.genres.map { it.id }.toSet()

            _state.value = MovieFormState.Idle
        }
    }


    /* ---------- Save (Create or Update) ---------- */

    fun save() {
        if (!validate()) return

        viewModelScope.launch {
            _state.value = MovieFormState.Loading

            val result = if (isEditMode) {
                repository.updateMovie(
                    id = movieId!!,
                    title = title.value!!,
                    synopsis = synopsis.value!!,
                    genres = selectedGenres.value!!.map { it.toInt() }.toSet(),
                    directorId = directorId.value,
                    releaseDate = releaseDate.value!!,
                    minimumAge = minimumAge.value!!
                )
            } else {
                val data = picture.value?.let { uri ->
                    val bytes = getApplication<App>().contentResolver.openInputStream(uri)?.use {
                        it.readBytes()
                    }

                    // bytes.encodeBase64()
                    bytes
                }

                repository.createMovie(
                    title = title.value!!,
                    synopsis = synopsis.value!!,
                    releaseDate = releaseDate.value!!,
                    minimumAge = minimumAge.value!!,
                    genres = selectedGenres.value!!.map { it.toInt() }.toSet(),
                    directorId = directorId.value,
                    pictures = data?.let {
                        listOf(
                            CreatePicture(
                                filename = "${title.value!!}.jpg",
                                data = it.encodeBase64(),
                                mainPicture = true
                            )
                        )
                    } ?: emptyList())
            }

            when (result) {
                is Resource.Success -> _state.value = MovieFormState.Saved
                is Resource.Error -> {
                    _state.value = MovieFormState.Error(result.problem)
                    Log.wtf("WTFIM.THROW", result.throwable.toString())
                    Log.wtf("WTFIM.TITLE", result.problem?.title)
                    Log.wtf("WTFIM.DETAIL", result.problem?.detail)
                    Log.wtf("WTFIM.TYPE", result.problem?.type)
                }

                else -> {}
            }
        }
    }

    /* ---------- Delete ---------- */

    fun delete() {
        if (!isEditMode) return

        viewModelScope.launch {
            _state.value = MovieFormState.Loading

            when (val result = repository.deleteMovie(movieId!!)) {
                is Resource.Success -> _state.value = MovieFormState.Deleted
                is Resource.Error -> _state.value = MovieFormState.Error(result.problem)
                else -> {}
            }
        }
    }

    /* ---------- Validation ---------- */

    private fun validate(): Boolean {
        if (title.value.isNullOrBlank()) {
            _state.value = MovieFormState.Error(ProblemDetails("400", "Title is required", 400, ""))
            return false
        }

//        if (releaseDate.value == null) {
//            _state.value =
//                MovieFormState.Error(ProblemDetails("400", "Release date is required", 400, ""))
//            return false
//        }

        return true
    }
}

class MovieUpsertViewModelFactory(
    private val app: Application, private val movieId: Long?
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST") return MovieUpsertViewModel(app, movieId) as T
    }
}


sealed class MovieFormState {
    object Idle : MovieFormState()
    object Loading : MovieFormState()
    object Saved : MovieFormState()
    object Deleted : MovieFormState()
    data class Error(val err: ProblemDetails?) : MovieFormState()
}
