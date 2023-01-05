package xyz.nowaha.chengetawildlife.data.pojo

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.*

@Entity
data class Account(
    @PrimaryKey
    @SerializedName("Id")
    var id: Int,
    @SerializedName("Username")
    var username: String,
    @SerializedName("Role")
    var role: Int,
    @SerializedName("CreationDate")
    var creationDate: Long,
    @SerializedName("FirstName")
    var firstName: String?,
    @SerializedName("LastName")
    var lastName: String?,

    // Local caching
    var cacheExpiry: Long?
)