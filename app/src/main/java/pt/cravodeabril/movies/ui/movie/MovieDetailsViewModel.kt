package pt.cravodeabril.movies.ui.movie

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.cravodeabril.movies.data.ApiResult
import pt.cravodeabril.movies.data.local.AppDatabase
import pt.cravodeabril.movies.data.remote.MovieRatingResponse
import pt.cravodeabril.movies.data.repository.MovieDetailsManual
import pt.cravodeabril.movies.data.repository.MovieRepository

class MovieDetailsViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase(app)
    private val repository = MovieRepository(db.movieDao())

    private val _movieDetails = MutableLiveData<MovieDetailsManual?>()
    val movieDetails: LiveData<MovieDetailsManual?> = _movieDetails

    private val _ratings = MutableLiveData<List<MovieRatingResponse>>()
    val ratings: LiveData<List<MovieRatingResponse>> = _ratings

    private val _ratingsLoading = MutableLiveData<Boolean>(false)
    val ratingsLoading: LiveData<Boolean> = _ratingsLoading

    fun loadMovieDetails(movieId: Long) {
        viewModelScope.launch {
            _movieDetails.postValue(repository.getMovieWithDetailsById(movieId))
        }
    }

    fun loadRatings(movieId: Long) {
        viewModelScope.launch {
            _ratingsLoading.postValue(true)
            try {
                repository.fetchMovieRatings(movieId).collect { result ->
                    when (result) {
                        is ApiResult.Success -> {
                            _ratings.postValue(result.data)
                            _ratingsLoading.postValue(false)
                        }

                        is ApiResult.Loading -> _ratingsLoading.postValue(true)
                        is ApiResult.Failure -> {
                            _ratings.postValue(emptyList())
                            _ratingsLoading.postValue(false)
                        }
                    }
                }
            } catch (_: Exception) {
                _ratings.postValue(emptyList())
                _ratingsLoading.postValue(false)
            }
        }
    }

    fun toggleFavorite(movieId: Long) {
        viewModelScope.launch {
            // TODO: PUT /movies/{id}/mark-as-favorite
        }
    }
}
