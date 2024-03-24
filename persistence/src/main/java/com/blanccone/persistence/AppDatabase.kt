package com.blanccone.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import com.blanccone.persistence.entity.TicketEntity

@Database(
    entities = [TicketEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase(){
    internal abstract fun appDao() : AppDao
}