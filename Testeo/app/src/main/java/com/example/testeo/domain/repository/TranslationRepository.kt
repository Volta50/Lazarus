package com.example.testeo.domain.repository

import com.example.testeo.data.local.SavedWord
import kotlinx.coroutines.flow.Flow

interface TranslationRepository {
    suspend fun saveWord(
        originalText: String,
        translatedText: String,
        sourceLangCode: String,
        targetLangCode: String
    )

    fun getAllWords(): Flow<List<SavedWord>>

    suspend fun getRandomWord(): SavedWord?

    fun translateText(text: String, sourceLang: String, targetLang: String): Flow<Result<String>>
}
