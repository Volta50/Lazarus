package com.example.lazarus.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: SavedWord)

    @Query("SELECT * FROM saved_words WHERE nextReviewTimestamp <= :currentTimeMillis")
    fun getWordsForReview(currentTimeMillis: Long): Flow<List<SavedWord>>

    @Query("SELECT * FROM saved_words")
    fun getAllWords(): Flow<List<SavedWord>>
}
