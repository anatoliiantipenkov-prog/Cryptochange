package com.cryptosignal.assistant.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cryptosignal.assistant.data.local.database.converters.Converters
import com.cryptosignal.assistant.data.local.database.dao.SignalDao
import com.cryptosignal.assistant.data.local.database.entity.SignalEntity

@Database(
    entities = [
        SignalEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun signalDao(): SignalDao
}