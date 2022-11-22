package xyz.nowaha.chengetawildlife.http

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import xyz.nowaha.chengetawildlife.pojo.*

interface APIInterface {

    @GET("/events/latest?")
    fun getLatestEvents(@Query("rows") rows: Int = 100, @Query("offset") offset: Int = 0) : Call<EventListResponse>

    @GET("/user/login?")
    fun attemptLogin(@Query("username") username: String, @Query("password") password: String) : Call<LoginResponse>

    @POST("/accounts/create")
    fun attemptCreateAccount(@Body request: AccountCreationRequest) : Call<AccountCreationResponse>

}