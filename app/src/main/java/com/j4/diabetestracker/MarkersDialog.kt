package com.j4.diabetestracker

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MarkersDialog(
    selectedLanguage: String,
    onMarkerCategories: () -> Unit,
    onCustomMarkers: () -> Unit,
    onDismiss: () -> Unit
) {
    val title = when (selectedLanguage) {
        "German" -> "Marker"
        "Spanish" -> "Marcadores"
        else -> "Markers"
    }

    SettingsCategoryDialog(
        icon = Icons.Default.Flag,
        title = title,
        onDismiss = onDismiss
    ) {
        Button(
            onClick = {
                onMarkerCategories()
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Icon(
                Icons.Default.Category,
                contentDescription = when (selectedLanguage) {
                    "German" -> "Marker-Kategorien"
                    "Spanish" -> "Categorías de marcadores"
                    else -> "Marker Categories"
                },
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = when (selectedLanguage) {
                    "German" -> "Marker-Kategorien"
                    "Spanish" -> "Categorías de marcadores"
                    else -> "Marker Categories"
                }
            )
        }

        Button(
            onClick = {
                onCustomMarkers()
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Icon(
                Icons.Default.Flag,
                contentDescription = when (selectedLanguage) {
                    "German" -> "Benutzerdefinierte Marker"
                    "Spanish" -> "Marcadores personalizados"
                    else -> "Custom Markers"
                },
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = when (selectedLanguage) {
                    "German" -> "Benutzerdefinierte Marker"
                    "Spanish" -> "Marcadores personalizados"
                    else -> "Custom Markers"
                }
            )
        }
    }
}
