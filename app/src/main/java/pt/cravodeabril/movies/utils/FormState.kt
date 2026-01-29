package pt.cravodeabril.movies.utils

import pt.cravodeabril.movies.data.ProblemDetails

sealed class FormState {
    object Idle : FormState()
    object Loading : FormState()
    object Saved : FormState()
    object Deleted : FormState()
    data class Error(val err: ProblemDetails?) : FormState()
}