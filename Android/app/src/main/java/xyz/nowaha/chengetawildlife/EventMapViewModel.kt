package xyz.nowaha.chengetawildlife

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import xyz.nowaha.chengetawildlife.http.APIClient
import xyz.nowaha.chengetawildlife.pojo.Event
import xyz.nowaha.chengetawildlife.pojo.EventListResponse

class EventMapViewModel : ViewModel() {

    val mapEvents = MutableLiveData<List<Event>>(arrayListOf())

    suspend fun loadEvents(): Boolean = withContext(Dispatchers.IO) {
        val data: Response<EventListResponse>
        try {
            data = APIClient.getAPIInterface().getLatestEvents(16).execute()
        } catch (_: Exception) {
            return@withContext false
        }

        if (data.body() != null && data.body()!!.data != null) {
            mapEvents.postValue(data.body()!!.data!!)
            return@withContext true
        }

        return@withContext false
    }

}