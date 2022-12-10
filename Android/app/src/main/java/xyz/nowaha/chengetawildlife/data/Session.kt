package xyz.nowaha.chengetawildlife.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Entity
data class Session(
    @PrimaryKey val primaryKey: Int = 0,
    val username: String,
    val sessionKey: String,
    val isAdmin: Boolean,
    val loginDate: Long
)

@Dao
interface SessionDao {

    @Query("SELECT * FROM session LIMIT 1;")
    fun get(): Session?

    @Query("SELECT * FROM session LIMIT 1;")
    fun getLiveData(): LiveData<Session?>

    @Delete
    fun delete(session: Session)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(session: Session)

}
