package com.example.testeo.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testeo.domain.repository.TranslationRepository
import com.google.mlkit.nl.translate.TranslateLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TranslatorViewModel @Inject constructor(
    private val repository: TranslationRepository
) : ViewModel() {

    private val _originalText = MutableStateFlow("")
    val originalText: StateFlow<String> = _originalText.asStateFlow()

    private val _translatedText = MutableStateFlow("")
    val translatedText: StateFlow<String> = _translatedText.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun onTextChanged(text: String) {
        _originalText.value = text
    }

    fun onTranslate() {
        if (_originalText.value.isBlank()) return

        _isLoading.value = true
        viewModelScope.launch {
            repository.translateText(
                text = _originalText.value,
                sourceLang = TranslateLanguage.ENGLISH,
                targetLang = TranslateLanguage.SPANISH
            ).collect { result ->
                _isLoading.value = false
                result.onSuccess {
                    _translatedText.value = it
                }.onFailure {
                    _translatedText.value = "Error: ${it.message}"
                }
            }
        }
    }

    fun onSave() {
        if (_originalText.value.isBlank() || _translatedText.value.isBlank()) return
        
        viewModelScope.launch {
            repository.saveWord(
                originalText = _originalText.value,
                translatedText = _translatedText.value,
                sourceLangCode = TranslateLanguage.ENGLISH,
                targetLangCode = TranslateLanguage.SPANISH
            )
        }
    }
}
