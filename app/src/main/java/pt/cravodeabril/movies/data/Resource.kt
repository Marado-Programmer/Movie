package pt.cravodeabril.movies.data

sealed interface Resource<out T> {
    object Loading : Resource<Nothing>

    data class Success<T>(val data: T) : Resource<T>

    data class Error(
        val throwable: Throwable? = null,
        val problem: ProblemDetails? = null
    ) : Resource<Nothing>
}