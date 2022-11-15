package xyz.nowaha.chengetawildlife.http

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object APIClient {

    private lateinit var retrofit: Retrofit
    private lateinit var apiInterface: APIInterface

    fun getClient(): Retrofit {
        if (this::retrofit.isInitialized) return retrofit

        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder().addInterceptor(interceptor).addInterceptor(BearerAuthInterceptor()).build()
        retrofit = Retrofit.Builder()
            .baseUrl("http://173.249.1.107:34100")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        return retrofit
    }

    fun getAPIInterface(): APIInterface {
        if (this::apiInterface.isInitialized) return apiInterface
        apiInterface = getClient().create(APIInterface::class.java)
        return apiInterface
    }

}