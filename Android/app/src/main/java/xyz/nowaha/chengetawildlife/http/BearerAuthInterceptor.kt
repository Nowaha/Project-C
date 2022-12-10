package xyz.nowaha.chengetawildlife.http

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import xyz.nowaha.chengetawildlife.SessionManager

class BearerAuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        return runBlocking {
            val session = SessionManager.getCurrentSession() ?: return@runBlocking chain.proceed(
                chain.request()
            )

            val request = chain.request().newBuilder()
            request.header("Authorization", session.sessionKey)
            return@runBlocking chain.proceed(request.build())
        }
    }

}