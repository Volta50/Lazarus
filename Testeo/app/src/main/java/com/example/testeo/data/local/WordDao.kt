package com.example.testeo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: SavedWord)

    @Query("SELECT * FROM saved_words ORDER BY createdAt DESC")
    fun getAllWords(): Flow<List<SavedWord>>

    @Query("SELECT * FROM saved_words ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomWord(): SavedWord?
}
