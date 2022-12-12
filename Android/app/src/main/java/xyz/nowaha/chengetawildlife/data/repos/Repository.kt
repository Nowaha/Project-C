package xyz.nowaha.chengetawildlife.data.repos

import android.content.Context
import xyz.nowaha.chengetawildlife.data.pojo.Event

interface Repository {

    val responseSource: RepoResponse.Source

    suspend fun getEvents(
        context: Context,
        rows: Int = 100,
        offset: Int = 0
    ): RepoResponse<List<Event>>

}