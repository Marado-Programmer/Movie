@file:OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
@file:UseContextualSerialization(Instant::class)
package pt.cravodeabril.movies.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi

sealed interface Resource<out T> {
    object Loading : Resource<Nothing>

    data class Success<T>(val data: T) : Resource<T>

    data class Error(
        val throwable: Throwable? = null,
        val problem: ProblemDetails? = null
    ) : Resource<Nothing>
}

@Serializable
data class ProblemDetails(
    val type: String,
    val title: String,
    val status: Int,
    val detail: String,
    val instance: String? = null
)