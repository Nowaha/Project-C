package xyz.nowaha.chengetawildlife.data.http

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import xyz.nowaha.chengetawildlife.data.pojo.*

interface APIInterface {

    @GET("/events/latest?")
    fun getLatestEvents(@Query("rows") rows: Int = 100, @Query("offset") offset: Int = 0) : Call<EventListResponse>

    @GET("/user/login?")
    fun attemptLogin(@Query("username") username: String, @Query("password") password: String) : Call<LoginResponse>

    @POST("/accounts/create")
    fun attemptCreateAccount(@Body request: AccountCreationRequest) : Call<AccountCreationResponse>

    @POST("/accounts/delete")
    fun deleteAccount(@Body request: AccountDeleteRequest) : Call<AccountDeleteResponse>

    @POST("/accounts/edit")
    fun attemptEditAccount(@Body request: AccountEditRequest) : Call<AccountEditResponse>
}