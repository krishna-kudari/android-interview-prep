package com.example.eventlogger.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.eventlogger.model.LogEvent

@Database(
    entities = [LogEvent::class],
    version = 1,
    exportSchema = true,
)
abstract class EventDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao

    companion object {
        private const val DB_NAME = "event_logger_db"

        fun create(context: Context): EventDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                EventDatabase::class.java,
                DB_NAME,
            ).build()
    }
}
