package xyz.nowaha.chengetawildlife.data

import androidx.room.*
import xyz.nowaha.chengetawildlife.data.pojo.Event

@Dao
interface EventDao {

    @Query("SELECT * FROM event ORDER BY date;")
    fun getAll(): List<Event>

    @Query("SELECT * FROM event WHERE id = :id LIMIT 1;")
    fun getById(id: Int): Event?

    @Query("SELECT * FROM event ORDER BY date DESC LIMIT :rows OFFSET :offset;")
    fun getLatest(rows: Int = 100, offset: Int = 0): List<Event>

    @Delete
    fun delete(event: Event)

    @Query("DELETE FROM event where id in (:idList)")
    fun deleteEvents(idList: List<Int>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(event: Event)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(event: List<Event>)

}