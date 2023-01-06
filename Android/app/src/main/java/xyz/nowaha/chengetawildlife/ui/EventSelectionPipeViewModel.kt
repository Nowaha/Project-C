package xyz.nowaha.chengetawildlife.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.*

class EventSelectionPipeViewModel : ViewModel() {
    val eventSelected = MutableLiveData(-1)
}