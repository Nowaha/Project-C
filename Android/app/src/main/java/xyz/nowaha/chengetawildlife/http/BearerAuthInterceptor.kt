package xyz.nowaha.chengetawildlife.http

import okhttp3.Interceptor
import okhttp3.Response
import xyz.nowaha.chengetawildlife.Session

class BearerAuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        if (Session.key == null) return chain.proceed(chain.request())

        val request = chain.request().newBuilder()
        request.header("Authorization", Session.key!!)
        return chain.proceed(request.build())
    }

}