package xyz.nowaha.chengetawildlife.http

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import xyz.nowaha.chengetawildlife.pojo.Event
import xyz.nowaha.chengetawildlife.pojo.EventListResponse
import xyz.nowaha.chengetawildlife.pojo.LoginResponse

interface APIInterface {

    @GET("/events/latest?")
    fun getLatestEvents(@Query("rows") rows: Int = 100, @Query("offset") offset: Int = 0) : Call<EventListResponse>

    @GET("/user/login?")
    fun attemptLogin(@Query("username") username: String, @Query("password") password: String) : Call<LoginResponse>

}