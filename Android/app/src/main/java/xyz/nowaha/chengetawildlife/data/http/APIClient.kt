package xyz.nowaha.chengetawildlife.data.http

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import xyz.nowaha.chengetawildlife.BuildConfig

object APIClient {

    private const val endpointURL = "http://173.249.1.107:34100"

    private lateinit var retrofit: Retrofit
    private lateinit var apiInterface: APIInterface

    fun getClient(): Retrofit {
        if (this::retrofit.isInitialized) return retrofit

        var clientBuilder = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) {
            val interceptor = HttpLoggingInterceptor()
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            clientBuilder = clientBuilder.addInterceptor(interceptor)
        }

        clientBuilder = clientBuilder.addInterceptor(BearerAuthInterceptor()).retryOnConnectionFailure(true)
        retrofit = Retrofit.Builder()
            .baseUrl(endpointURL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(clientBuilder.build())
            .build()

        return retrofit
    }

    fun getAPIInterface(): APIInterface {
        if (this::apiInterface.isInitialized) return apiInterface
        apiInterface = getClient().create(APIInterface::class.java)
        return apiInterface
    }

}