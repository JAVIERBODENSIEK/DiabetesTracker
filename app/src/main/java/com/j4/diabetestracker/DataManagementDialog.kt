package com.j4.diabetestracker

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun DataManagementDialog(
    selectedLanguage: String,
    onManageCustomColumns: () -> Unit,
    onDismiss: () -> Unit
) {
    val title = when (selectedLanguage) {
        "German" -> "Datenverwaltung"
        "Spanish" -> "Gestión de datos"
        else -> "Data Management"
    }

    SettingsCategoryDialog(
        icon = Icons.Default.Storage,
        title = title,
        onDismiss = onDismiss
    ) {
        Button(
            onClick = {
                onManageCustomColumns()
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Icon(
                Icons.Default.ViewColumn,
                contentDescription = stringResource(R.string.manage_custom_columns),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.manage_custom_columns))
        }
    }
}
