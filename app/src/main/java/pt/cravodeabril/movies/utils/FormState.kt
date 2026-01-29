package pt.cravodeabril.movies.utils

import pt.cravodeabril.movies.data.ProblemDetails

/**
 * On creation, edition or deletion tasks, this represents the state of the task for the UI to consume
 */
sealed class FormState {
    object Idle : FormState()
    object Loading : FormState()
    object Saved : FormState()
    object Deleted : FormState()
    data class Error(val err: ProblemDetails?) : FormState()
}