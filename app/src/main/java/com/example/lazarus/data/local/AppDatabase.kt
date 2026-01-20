package com.example.lazarus.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SavedWord::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
}
