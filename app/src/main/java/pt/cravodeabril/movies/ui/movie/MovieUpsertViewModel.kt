package pt.cravodeabril.movies.ui.movie

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import pt.cravodeabril.movies.data.ApiResult
import pt.cravodeabril.movies.data.ProblemDetails
import pt.cravodeabril.movies.data.local.AppDatabase
import pt.cravodeabril.movies.data.repository.MovieRepository

class MovieUpsertViewModel(app: Application, private val movieId: Long? = null) :
    AndroidViewModel(app) {
    private val db = AppDatabase(app)
    private val repository = MovieRepository(db.movieDao(), db.genreDao(), db.personDao())

    val isEditMode: Boolean = movieId != null

    /* ---------- Form fields ---------- */

    val title = MutableLiveData("")
    val synopsis = MutableLiveData("")
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
            if (refreshResult is ApiResult.Failure) {
                _state.value = MovieFormState.Error(refreshResult.error)
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
                repository.createMovie(
                    title = title.value!!,
                    synopsis = synopsis.value!!,
                    genres = selectedGenres.value!!.map { it.toInt() }.toSet(),
                    directorId = directorId.value,
                    releaseDate = releaseDate.value!!,
                    minimumAge = minimumAge.value!!
                )
            }

            when (result) {
                is ApiResult.Success -> _state.value = MovieFormState.Saved
                is ApiResult.Failure -> _state.value = MovieFormState.Error(result.error)
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
                is ApiResult.Success -> _state.value = MovieFormState.Deleted
                is ApiResult.Failure -> _state.value = MovieFormState.Error(result.error)
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

        if (releaseDate.value == null) {
            _state.value =
                MovieFormState.Error(ProblemDetails("400", "Release date is required", 400, ""))
            return false
        }

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
    data class Error(val err: ProblemDetails) : MovieFormState()
}
