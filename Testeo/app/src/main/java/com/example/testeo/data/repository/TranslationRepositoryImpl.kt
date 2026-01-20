package com.example.testeo.data.repository

import com.example.testeo.data.local.SavedWord
import com.example.testeo.data.local.WordDao
import com.example.testeo.domain.repository.TranslationRepository
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class TranslationRepositoryImpl @Inject constructor(
    private val wordDao: WordDao
) : TranslationRepository {

    override suspend fun saveWord(
        originalText: String,
        translatedText: String,
        sourceLangCode: String,
        targetLangCode: String
    ) {
        val word = SavedWord(
            originalText = originalText,
            translatedText = translatedText,
            sourceLangCode = sourceLangCode,
            targetLangCode = targetLangCode
        )
        wordDao.insertWord(word)
    }

    override fun getAllWords(): Flow<List<SavedWord>> {
        return wordDao.getAllWords()
    }

    override suspend fun getRandomWord(): SavedWord? {
        return wordDao.getRandomWord()
    }

    override fun translateText(
        text: String,
        sourceLang: String,
        targetLang: String
    ): Flow<Result<String>> = callbackFlow {
        tryCall {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceLang)
                .setTargetLanguage(targetLang)
                .build()

            val translator = Translation.getClient(options)

            val conditions = DownloadConditions.Builder()
                .requireWifi()
                .build()

            translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener {
                    translator.translate(text)
                        .addOnSuccessListener { translatedText ->
                            trySend(Result.success(translatedText))
                            close()
                        }
                        .addOnFailureListener { exception ->
                            trySend(Result.failure(exception))
                            close()
                        }
                }
                .addOnFailureListener { exception ->
                    trySend(Result.failure(exception))
                    close()
                }
            
            awaitClose { translator.close() }
        }
    }

    private inline fun <T> ProducerScope<T>.tryCall(block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            trySend(Result.failure<Any>(e) as T)
            close(e)
        }
    }
}
