package xyz.nowaha.chengetawildlife.data.repos

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xyz.nowaha.chengetawildlife.data.pojo.Event

object Repositories {

    val offlineRepository = LocalRepository()
    val remoteRepository = RemoteRepository()

    suspend fun <T> fetch(
        local: suspend () -> RepoResponse<T>,
        remote: suspend () -> RepoResponse<T>,
        cache: suspend (T) -> Unit,
        cacheRules: CacheRules
    ): RepoResponse<T> = withContext(Dispatchers.IO) {
        if (cacheRules.mode == CacheRules.CacheMode.NORMAL) {
            val localResponse = local()
            if (localResponse.responseType == RepoResponse.ResponseType.SUCCESS) return@withContext localResponse
        }

        val remoteResponse = remote()
        if (cacheRules.mode != CacheRules.CacheMode.NO_CACHE) {
            cache(remoteResponse.result)
        }
        return@withContext remoteResponse
    }

    suspend fun getEvents(
        context: Context, rows: Int = 100, offset: Int = 0, cacheRules: CacheRules = CacheRules()
    ): RepoResponse<List<Event>> = withContext(Dispatchers.IO) {
        fetch(
            local = { offlineRepository.getEvents(context, rows, offset) },
            remote = { remoteRepository.getEvents(context, rows, offset) },
            cache = { offlineRepository.cacheEvents(it, cacheRules) },
            cacheRules = cacheRules
        )
    }

    data class CacheRules(
        val mode: CacheMode = CacheMode.NORMAL, val cacheDuration: Long = 360000
    ) {
        enum class CacheMode {
            NORMAL, REFRESH, NO_CACHE, DELETE
        }
    }

    @SuppressLint("MissingPermission")
    fun Context.isNetworkAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nw = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            //for other device how are able to connect with Ethernet
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            //for check internet over Bluetooth
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
            else -> false
        }
    }

}