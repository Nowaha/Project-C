package xyz.nowaha.chengetawildlife.pojo

import com.google.gson.annotations.SerializedName
import java.util.*

data class Event(
    @SerializedName("Id")
    var id: Int,
    @SerializedName("NodeID")
    var nodeId: Int,
    @SerializedName("Date")
    var date: Long,
    @SerializedName("Latitude")
    var latitude: Float,
    @SerializedName("Longitude")
    var longitude: Float,
    @SerializedName("SoundLabel")
    var soundLabel: String,
    @SerializedName("Probability")
    var probability: Int,
    @SerializedName("SoundURL")
    var soundUrl: String
)