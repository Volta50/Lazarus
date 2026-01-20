package com.example.lazarus.ui.translator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lazarus.domain.repository.TranslationRepository
import com.google.mlkit.nl.translate.TranslateLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TranslatorUiState(
    val inputText: String = "",
    val translatedText: String = "",
    val sourceLangCode: String = TranslateLanguage.ENGLISH,
    val targetLangCode: String = TranslateLanguage.SPANISH, // Defaulting to Spanish
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)

@HiltViewModel
class TranslatorViewModel @Inject constructor(
    private val repository: TranslationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TranslatorUiState())
    val uiState: StateFlow<TranslatorUiState> = _uiState.asStateFlow()

    private var translationJob: Job? = null

    fun onTextChanged(text: String) {
        _uiState.update { it.copy(inputText = text, isSaved = false) }
        // Debounce translation
        translationJob?.cancel()
        translationJob = viewModelScope.launch {
            delay(500) // Debounce 500ms
            if (text.isNotBlank()) {
                translate()
            }
        }
    }

    fun swapLanguages() {
        _uiState.update {
            it.copy(
                sourceLangCode = it.targetLangCode,
                targetLangCode = it.sourceLangCode,
                inputText = it.translatedText,
                translatedText = it.inputText, // This might be weird if translation isn't perfect, but standard for swap
                isSaved = false
            )
        }
        // Trigger translation after swap if there's text
        if (_uiState.value.inputText.isNotBlank()) {
            translate()
        }
    }

    fun translate() {
        val state = _uiState.value
        if (state.inputText.isBlank()) return

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val result = repository.translateText(
                text = state.inputText,
                sourceLang = state.sourceLangCode,
                targetLang = state.targetLangCode
            )
            
            result.onSuccess { translated ->
                _uiState.update { it.copy(translatedText = translated, isLoading = false) }
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message ?: "Unknown error", isLoading = false) }
            }
        }
    }

    fun saveCurrentWord() {
        val state = _uiState.value
        if (state.inputText.isBlank() || state.translatedText.isBlank()) return

        viewModelScope.launch {
            repository.saveWord(
                original = state.inputText,
                translated = state.translatedText,
                sourceLang = state.sourceLangCode,
                targetLang = state.targetLangCode
            )
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
