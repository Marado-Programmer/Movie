@file:OptIn(ExperimentalUuidApi::class)

package pt.cravodeabril.movies.ui.person

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.cravodeabril.movies.App
import pt.cravodeabril.movies.data.Resource
import pt.cravodeabril.movies.data.local.entity.FullPersonEntity
import kotlin.uuid.ExperimentalUuidApi

class PersonListViewModel(app: Application) : AndroidViewModel(app) {
    private val appContainer = (app as App).container

    private val repository = appContainer.movieRepository

    private val _people = MutableLiveData<Resource<List<FullPersonEntity>>>()
    val people: LiveData<Resource<List<FullPersonEntity>>> = _people

    init {
        observePeople()
        refresh()
    }

    fun observePeople(query: String = "") {
        viewModelScope.launch {
            repository.observePeople().collect { people ->
                _people.postValue(Resource.Success(people.filter { person ->
                    query == "" || person.person.name.contains(
                        query
                    )
                }))
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            when (val result = repository.refreshPeople()) {
                is Resource.Error -> _people.postValue(result)
                else -> Unit
            }
        }
    }

    fun deletePerson(id: Long) {
        viewModelScope.launch {
            repository.deletePerson(id)
        }
    }
}