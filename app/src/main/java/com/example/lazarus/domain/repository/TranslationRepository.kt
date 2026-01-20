package com.example.lazarus.domain.repository

import com.example.lazarus.data.local.SavedWord
import kotlinx.coroutines.flow.Flow

interface TranslationRepository {
    suspend fun translateText(text: String, sourceLang: String, targetLang: String): Result<String>
    suspend fun saveWord(original: String, translated: String, sourceLang: String, targetLang: String)
    fun getWordsForReview(): Flow<List<SavedWord>>
}
