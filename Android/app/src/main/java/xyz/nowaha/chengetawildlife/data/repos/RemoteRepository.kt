package xyz.nowaha.chengetawildlife.data.repos

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xyz.nowaha.chengetawildlife.data.http.APIClient
import xyz.nowaha.chengetawildlife.data.pojo.Event

class RemoteRepository : Repository {

    override val responseSource: RepoResponse.Source = RepoResponse.Source.REMOTE

    override suspend fun getEvents(
        context: Context,
        rows: Int,
        offset: Int
    ): RepoResponse<List<Event>> = withContext(Dispatchers.IO) {
        try {
            val response = APIClient.getAPIInterface().getLatestEvents(rows, offset).execute()
            return@withContext if (response.isSuccessful && response.body() != null) {
                RepoResponse(
                    RepoResponse.ResponseType.SUCCESS, response.body()!!.data!!, responseSource
                )
            } else {
                val responseType = when (response.code()) {
                    404 -> RepoResponse.ResponseType.NOT_FOUND
                    401 -> RepoResponse.ResponseType.UNAUTHORIZED
                    else -> RepoResponse.ResponseType.UNKNOWN_ERROR
                }

                RepoResponse(responseType, arrayListOf(), responseSource)
            }
        } catch (exception: Exception) {
            return@withContext RepoResponse(
                RepoResponse.ResponseType.CONNECTION_ERROR, arrayListOf(), responseSource
            )
        }
    }

}