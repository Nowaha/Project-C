package xyz.nowaha.chengetawildlife.ui.map

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xyz.nowaha.chengetawildlife.data.pojo.Event
import xyz.nowaha.chengetawildlife.data.repos.RepoResponse
import xyz.nowaha.chengetawildlife.data.repos.Repositories

class EventMapViewModel : ViewModel() {

    var selectedEvent: Int? = null
    var firstLoad = true

}