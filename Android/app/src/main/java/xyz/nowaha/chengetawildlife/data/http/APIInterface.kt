package xyz.nowaha.chengetawildlife.data.http

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import xyz.nowaha.chengetawildlife.data.pojo.*

interface APIInterface {

    @GET("/events/latest?")
    fun getLatestEvents(
        @Query("rows") rows: Int = 100,
        @Query("offset") offset: Int = 0
    ): Call<EventListResponse>

    @GET("/user/login?")
    fun attemptLogin(
        @Query("username") username: String,
        @Query("password") password: String
    ): Call<LoginResponse>

    @GET("/accounts/session/validate")
    fun validateSession(): Call<SessionValidationResponse>

    @GET("/accounts/view?")
    fun getAccountList(
        @Query("rows") rows: Int = 100,
        @Query("offset") offset: Int = 0
    ): Call<AccountListResponse>

    @GET("/accounts/view?")
    fun getAccountListByUsername(
        @Query("rows") rows: Int = 100,
        @Query("offset") offset: Int = 0,
        @Query("username") username: String
    ): Call<AccountListResponse>

    @POST("/accounts/create")
    fun attemptCreateAccount(@Body request: AccountCreationRequest): Call<AccountCreationResponse>

    @POST("/accounts/delete")
    fun deleteAccount(@Body request: AccountDeleteRequest): Call<AccountDeleteResponse>

    @POST("/accounts/edit")
    fun attemptEditAccount(@Body request: AccountEditRequest) : Call<AccountEditResponse>

    @POST("accounts/edit/name")
    fun attemptEditNameAccount(@Body request: AccountEditNameRequest) : Call<AccountEditNameResponse>

    @POST("/events/status")
    fun attemptEditEventStatus(@Body request: EventEditTaskRequest): Call<EventEditTaskResponse>
}