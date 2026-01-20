package com.example.lazarus.data.repository

import com.example.lazarus.data.local.SavedWord
import com.example.lazarus.data.local.WordDao
import com.example.lazarus.domain.repository.TranslationRepository
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class TranslationRepositoryImpl @Inject constructor(
    private val wordDao: WordDao
) : TranslationRepository {

    override suspend fun translateText(
        text: String,
        sourceLang: String,
        targetLang: String
    ): Result<String> = try {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLang)
            .setTargetLanguage(targetLang)
            .build()
        
        val translator = Translation.getClient(options)
        
        // Use suspendCancellableCoroutine to bridge callback-based API to Coroutines
        kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
            var downloadStarted = false
            
            // First check if model is downloaded (optional optimization, but good practice)
            val conditions = DownloadConditions.Builder()
                .requireWifi()
                .build()

            translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener {
                    downloadStarted = true
                    // Model downloaded (or already available), proceed to translate
                    translator.translate(text)
                        .addOnSuccessListener { translatedText ->
                            if (continuation.isActive) {
                                continuation.resume(Result.success(translatedText))
                            }
                            translator.close() // Close strictly after usage for this simple scope
                        }
                        .addOnFailureListener { exception ->
                            if (continuation.isActive) {
                                continuation.resume(Result.failure(exception))
                            }
                            translator.close()
                        }
                }
                .addOnFailureListener { exception ->
                    if (continuation.isActive) {
                         continuation.resume(Result.failure(exception))
                    }
                    translator.close()
                }
                
             continuation.invokeOnCancellation { 
                 translator.close()
             }
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun saveWord(
        original: String,
        translated: String,
        sourceLang: String,
        targetLang: String
    ) {
        val word = SavedWord(
            originalText = original,
            translatedText = translated,
            sourceLang = sourceLang,
            targetLang = targetLang,
            nextReviewTimestamp = System.currentTimeMillis() // Ready for review immediately for now
        )
        wordDao.insertWord(word)
    }

    override fun getWordsForReview(): Flow<List<SavedWord>> {
        return wordDao.getWordsForReview(System.currentTimeMillis())
    }
}
