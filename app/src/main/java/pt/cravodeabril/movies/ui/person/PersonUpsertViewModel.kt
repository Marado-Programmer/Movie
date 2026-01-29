package pt.cravodeabril.movies.ui.person

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.ktor.util.encodeBase64
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import pt.cravodeabril.movies.App
import pt.cravodeabril.movies.data.ProblemDetails
import pt.cravodeabril.movies.data.Resource
import pt.cravodeabril.movies.data.remote.CreatePicture
import pt.cravodeabril.movies.utils.FormState
import java.io.FileNotFoundException

class PersonUpsertViewModel(app: Application, private val id: Long? = null) :
    AndroidViewModel(app) {
    private val appContainer = getApplication<App>().container

    private val repository = appContainer.movieRepository

    val isEditMode: Boolean = id != null

    val name = MutableLiveData("")
    val dateOfBirth = MutableLiveData<LocalDate>()
    val picture = MutableLiveData<Uri>()

    private val _state = MutableLiveData<FormState>(FormState.Idle)
    val state: LiveData<FormState> = _state

    init {
        if (isEditMode) load()
    }

    private fun load() {
        viewModelScope.launch {
            _state.value = FormState.Loading

            val refreshResult = repository.refreshPeople()
            if (refreshResult is Resource.Error) {
                _state.value = FormState.Error(refreshResult.problem)
                return@launch
            }

            val person = repository.observePeople().firstOrNull()
                ?.firstOrNull { person -> person.person.id == id }

            if (person == null) {
                _state.value = FormState.Error(
                    ProblemDetails("404", "Person not found", 404, "")
                )
                return@launch
            }

            name.value = person.person.name
            dateOfBirth.value = person.person.dateOfBirth
            id?.let {
                picture.value = Uri.parse(pictureUrl(it, person.pictures.first().id))
            }

            _state.value = FormState.Idle
        }
    }

    fun save() {
        if (!validate()) return

        viewModelScope.launch {
            _state.value = FormState.Loading


            val data = picture.value?.let { uri ->
                try {
                    val bytes = getApplication<App>().contentResolver.openInputStream(uri)?.use {
                        it.readBytes()
                    }

                    // bytes.encodeBase64()
                    bytes
                } catch (_: FileNotFoundException) {
                    null
                }
            }

            val result = if (isEditMode) {
                var res = repository.updatePerson(
                    id = id!!, name = name.value!!, dateOfBirth = dateOfBirth.value
                )

                data?.let { pic ->
                    res = repository.updatePersonPicture(
                        id, CreatePicture(
                            filename = "${name.value!!}.jpg",
                            data = pic.encodeBase64(),
                            mainPicture = true
                        )
                    )
                }

                res
            } else {
                repository.createPerson(
                    name = name.value!!,
                    dateOfBirth = dateOfBirth.value,
                    pictures = data?.let {
                        listOf(
                            CreatePicture(
                                filename = "${name.value!!}.jpg",
                                data = it.encodeBase64(),
                                mainPicture = true
                            )
                        )
                    } ?: emptyList())
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

        Log.v("RES", id.toString())
        Log.v("RES", isEditMode.toString())
        if (!isEditMode) return

        viewModelScope.launch {
            _state.value = FormState.Loading
            val result = repository.deletePerson(id!!)
            Log.v("RES", result.toString())
            when (result) {
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

    fun pictureUrl(id: Long, picId: Long): String =
        "http://10.0.2.2:8080/people/$id/picture/$picId"
}