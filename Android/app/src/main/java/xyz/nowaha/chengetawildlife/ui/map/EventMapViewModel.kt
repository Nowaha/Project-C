package xyz.nowaha.chengetawildlife.ui.map

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xyz.nowaha.chengetawildlife.data.pojo.Event
import xyz.nowaha.chengetawildlife.data.repos.RepoResponse
import xyz.nowaha.chengetawildlife.data.repos.Repositories

class EventMapViewModel : ViewModel() {

    val mapEvents = MutableLiveData<List<Event>>(arrayListOf())
    var selectedEvent: Int? = null
    var tableFragmentCreated = false

    suspend fun loadEvents(context: Context): Boolean = withContext(Dispatchers.IO) {
        val response = Repositories.getEvents(context, 16, 0)

        if (response.responseType == RepoResponse.ResponseType.SUCCESS) {
            mapEvents.postValue(response.result)
            return@withContext true
        }

        return@withContext false
    }

}