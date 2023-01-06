package xyz.nowaha.chengetawildlife.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.nowaha.chengetawildlife.data.pojo.Event
import xyz.nowaha.chengetawildlife.data.repos.RepoResponse
import xyz.nowaha.chengetawildlife.data.repos.Repositories

class EventDataViewModel(application: Application) : AndroidViewModel(application) {

    var data: MutableLiveData<EventData> = MutableLiveData(EventData(RepoResponse.ResponseType.SUCCESS, arrayListOf()))
    var state: MutableLiveData<EventDataState> = MutableLiveData(EventDataState.Idle)

    private val updateInterval = 1000 * 30
    var lastUpdate = System.currentTimeMillis()

    init {
        if (data.value == null || data.value!!.data.isEmpty()) update()

        viewModelScope.launch {
            while (true) {
                delay(5000)
                if (System.currentTimeMillis() - lastUpdate >= updateInterval) {
                    update()
                }
            }
        }
    }

    fun update() = viewModelScope.launch(Dispatchers.IO) {
        if (state.value != EventDataState.Idle) return@launch
        state.postValue(EventDataState.Loading)
        lastUpdate = System.currentTimeMillis()

        val repoResponse = Repositories.getEvents(getApplication<Application>(), 100, 0)
        data.postValue(EventData(repoResponse.responseType, repoResponse.result))
        state.postValue(EventDataState.Done)
        delay(500)
        state.postValue(EventDataState.Idle)
    }

    data class EventData(val lastResponse: RepoResponse.ResponseType, val data: List<Event>)

    sealed class EventDataState {
        object Idle : EventDataState()
        object Loading : EventDataState()
        object Done : EventDataState()
    }

}