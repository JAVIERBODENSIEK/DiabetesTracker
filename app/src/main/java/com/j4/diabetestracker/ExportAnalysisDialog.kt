package com.j4.diabetestracker

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun ExportAnalysisDialog(
    selectedLanguage: String,
    onExportPdf: () -> Unit,
    onExportTxt: () -> Unit,
    onDismiss: () -> Unit
) {
    val title = when (selectedLanguage) {
        "German" -> "Export"
        "Spanish" -> "Exportar"
        else -> "Export"
    }

    SettingsCategoryDialog(
        icon = Icons.Default.Description,
        title = title,
        onDismiss = onDismiss
    ) {
        Button(
            onClick = {
                onExportPdf()
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Text(stringResource(R.string.export_pdf))
        }

        Button(
            onClick = {
                onExportTxt()
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Text(stringResource(R.string.export_txt))
        }
    }
}
