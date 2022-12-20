package xyz.nowaha.chengetawildlife.data.pojo

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.*

@Entity
data class Event(
    @PrimaryKey
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
    var soundUrl: String,
    @SerializedName("Status")
    var status: String,
    // Local caching
    var cacheExpiry: Long?
)