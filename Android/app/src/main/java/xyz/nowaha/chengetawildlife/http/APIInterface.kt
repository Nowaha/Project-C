package xyz.nowaha.chengetawildlife.http

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import xyz.nowaha.chengetawildlife.pojo.Event
import xyz.nowaha.chengetawildlife.pojo.EventListResponse

interface APIInterface {

    @GET("/events/latest?")
    fun getLatestEvents(@Query("rows") rows: Int = 100, @Query("offset") offset: Int = 0) : Call<EventListResponse>

}