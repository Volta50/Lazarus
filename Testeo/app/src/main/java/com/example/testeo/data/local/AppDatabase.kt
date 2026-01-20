package com.example.testeo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.testeo.data.local.SavedWord
import com.example.testeo.data.local.WordDao

@Database(entities = [SavedWord::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
}
