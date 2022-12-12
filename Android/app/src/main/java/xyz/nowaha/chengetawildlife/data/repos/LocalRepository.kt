package xyz.nowaha.chengetawildlife.data.repos

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xyz.nowaha.chengetawildlife.MainActivity
import xyz.nowaha.chengetawildlife.data.pojo.Event

class LocalRepository : Repository {

    override val responseSource: RepoResponse.Source = RepoResponse.Source.LOCAL

    override suspend fun getEvents(
        context: Context,
        rows: Int,
        offset: Int
    ): RepoResponse<List<Event>> =
        withContext(Dispatchers.IO) {
            val eventDao = MainActivity.appDatabase?.eventDao()
            val events = eventDao?.getLatest(rows, offset)

            if (events != null) {
                if (!MainActivity.offlineModePrecise(context)) {
                    return@withContext RepoResponse(
                        RepoResponse.ResponseType.EXPIRED, arrayListOf(), responseSource
                    )
                }

                RepoResponse(
                    RepoResponse.ResponseType.SUCCESS, events, responseSource
                )
            } else {
                return@withContext RepoResponse(
                    RepoResponse.ResponseType.NOT_FOUND, arrayListOf(), responseSource
                )
            }
        }

    fun cacheEvents(events: List<Event>, rules: Repositories.CacheRules) {
        MainActivity.appDatabase?.eventDao()?.let {
            it.deleteEvents(it.getAll().map { it.id })
            it.insertAll(events)
        }
    }

}