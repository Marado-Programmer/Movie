package pt.cravodeabril.movies.data

sealed interface ApiResult<out T> {
    object Loading : ApiResult<Nothing>
    data class Success<T>(val data: T) : ApiResult<T>
    data class Failure(val error: ProblemDetails) : ApiResult<Nothing>
}