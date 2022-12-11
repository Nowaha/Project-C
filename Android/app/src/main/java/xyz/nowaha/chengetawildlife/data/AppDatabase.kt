package xyz.nowaha.chengetawildlife.data

import androidx.room.Database
import androidx.room.RoomDatabase
import xyz.nowaha.chengetawildlife.pojo.Event

@Database(entities = [Session::class, Event::class], version = 2)
abstract class AppDatabase : RoomDatabase() {

    abstract fun sessionDao(): SessionDao
    abstract fun eventDao(): EventDao

}