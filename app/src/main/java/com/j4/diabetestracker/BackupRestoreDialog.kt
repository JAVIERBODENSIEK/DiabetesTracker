package com.j4.diabetestracker

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun BackupRestoreDialog(
    selectedLanguage: String,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
    onDismiss: () -> Unit
) {
    val title = when (selectedLanguage) {
        "German" -> "Sicherung & Wiederherstellung"
        "Spanish" -> "Copia de seguridad y restauración"
        else -> "Backup & Restore"
    }

    SettingsCategoryDialog(
        icon = Icons.Default.CloudSync,
        title = title,
        onDismiss = onDismiss
    ) {
        Button(
            onClick = {
                onExportBackup()
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Icon(
                Icons.Default.Upload,
                contentDescription = stringResource(R.string.export_backup),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.export_backup))
        }

        Button(
            onClick = {
                onImportBackup()
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary
            )
        ) {
            Icon(
                Icons.Default.Download,
                contentDescription = stringResource(R.string.import_backup),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.import_backup))
        }
    }
}
