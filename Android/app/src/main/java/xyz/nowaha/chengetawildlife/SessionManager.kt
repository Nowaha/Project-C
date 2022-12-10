package xyz.nowaha.chengetawildlife

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xyz.nowaha.chengetawildlife.data.AppDatabase
import xyz.nowaha.chengetawildlife.data.Session

object SessionManager {

    private lateinit var database: AppDatabase

    fun initDatabase(context: Context) {
        database = Room.databaseBuilder(context, AppDatabase::class.java, "chengeta-db")
            .fallbackToDestructiveMigration().build()
    }

    suspend fun newLogin(session: Session) = withContext(Dispatchers.IO) {
        database.sessionDao().insert(session)
    }

    suspend fun logOut() = withContext(Dispatchers.IO) {
        getCurrentSession()?.let { database.sessionDao().delete(it) }
    }

    suspend fun getCurrentSession(): Session? =
        withContext(Dispatchers.IO) { database.sessionDao().get() }

    fun getCurrentSessionLiveData(): LiveData<Session?> = database.sessionDao().getLiveData()

}