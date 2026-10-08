package com.j4.diabetestracker

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun LanguageSettingsDialog(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val title = when (selectedLanguage) {
        "German" -> "Sprache"
        "Spanish" -> "Idioma"
        "French" -> "Langue"
        "Italian" -> "Lingua"
        "Portuguese" -> "Idioma"
        "Dutch" -> "Taal"
        "Russian" -> "Язык"
        "Japanese" -> "言語"
        "Chinese" -> "语言"
        "Hindi" -> "भाषा"
        "Arabic" -> "اللغة"
        "Bengali" -> "ভাষা"
        else -> "Language"
    }

    SettingsCategoryDialog(
        icon = Icons.Default.Language,
        title = title,
        onDismiss = onDismiss
    ) {
        Text(
            text = when (selectedLanguage) {
                "German" -> "Sprache auswählen"
                "Spanish" -> "Seleccionar idioma"
                else -> "Select Language"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        val languages = listOf(
            "English" to "🇬🇧",
            "German" to "🇩🇪",
            "Spanish" to "🇪🇸",
            "French" to "🇫🇷",
            "Italian" to "🇮🇹",
            "Portuguese" to "🇵🇹",
            "Dutch" to "🇳🇱",
            "Russian" to "🇷🇺",
            "Japanese" to "🇯🇵",
            "Chinese" to "🇨🇳",
            "Hindi" to "🇮🇳",
            "Arabic" to "🇸🇦",
            "Bengali" to "🇧🇩"
        )

        languages.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { (lang, flag) ->
                    val isSelected = lang == selectedLanguage
                    Surface(
                        onClick = {
                            onLanguageSelected(lang)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = flag, style = MaterialTheme.typography.headlineSmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = lang,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    }
                }
                // Fill remaining space if row has fewer than 3 items
                repeat(3 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
