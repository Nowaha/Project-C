package xyz.nowaha.chengetawildlife

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xyz.nowaha.chengetawildlife.data.Session

object SessionManager {

    suspend fun newLogin(session: Session) = withContext(Dispatchers.IO) {
        MainActivity.appDatabase?.sessionDao()?.insert(session)
    }

    suspend fun logOut() = withContext(Dispatchers.IO) {
        getCurrentSession()?.let { MainActivity.appDatabase?.sessionDao()?.delete(it) }
    }

    suspend fun getCurrentSession(): Session? =
        withContext(Dispatchers.IO) { MainActivity.appDatabase?.sessionDao()?.get() }

    fun getCurrentSessionLiveData(): LiveData<Session?> =
        MainActivity.appDatabase!!.sessionDao().getLiveData()

}