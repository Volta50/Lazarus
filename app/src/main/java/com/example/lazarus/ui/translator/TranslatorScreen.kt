package com.example.lazarus.ui.translator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.mlkit.nl.translate.TranslateLanguage
import java.util.Locale

@Composable
fun TranslatorScreen(
    viewModel: TranslatorViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LanguageSelector(
                sourceLangCode = state.sourceLangCode,
                targetLangCode = state.targetLangCode,
                onSwapLanguages = viewModel::swapLanguages
            )

            OutlinedTextField(
                value = state.inputText,
                onValueChange = viewModel::onTextChanged,
                label = { Text("Enter text") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            if (state.isLoading) {
                CircularProgressIndicator()
            }

            if (state.error != null) {
                Text(
                    text = "Error: ${state.error}",
                    color = MaterialTheme.colorScheme.error
                )
            }

            OutlinedTextField(
                value = state.translatedText,
                onValueChange = {}, // Read-only
                label = { Text("Translation") },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = viewModel::saveCurrentWord,
                enabled = state.translatedText.isNotBlank() && !state.isSaved,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.isSaved) "Saved to Flashcards" else "Save to Flashcards")
            }
        }
    }
}

@Composable
fun LanguageSelector(
    sourceLangCode: String,
    targetLangCode: String,
    onSwapLanguages: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        LanguageBadge(langCode = sourceLangCode)
        Spacer(modifier = Modifier.width(16.dp))
        IconButton(onClick = onSwapLanguages) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                contentDescription = "Swap languages"
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        LanguageBadge(langCode = targetLangCode)
    }
}

@Composable
fun LanguageBadge(langCode: String) {
    // Simple way to get display name
    val locale = Locale(langCode)
    
    // Fallback manual checks for common ML Kit codes that might not map perfectly to Locale if needed
    // But Locale constructor usually works fine for 'en', 'es', etc.
    val displayName = locale.displayLanguage.ifBlank { langCode }

    Text(
        text = displayName.uppercase(),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
}
