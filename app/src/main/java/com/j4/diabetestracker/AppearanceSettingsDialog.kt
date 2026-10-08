package com.j4.diabetestracker

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun AppearanceSettingsDialog(
    selectedLanguage: String,
    context: Context,
    isDarkMode: Boolean,
    onDarkModeChanged: (Boolean) -> Unit,
    fontColor: Color,
    backgroundColor: Color,
    gridColor: Color,
    insulinColumnColor: Color,
    onFontColorChanged: (Color) -> Unit,
    onBackgroundColorChanged: (Color) -> Unit,
    onGridColorChanged: (Color) -> Unit,
    onInsulinColumnColorChanged: (Color) -> Unit,
    onResetColors: () -> Unit,
    onShowColorSchemeDialog: () -> Unit,
    onDismiss: () -> Unit
) {
    val title = when (selectedLanguage) {
        "German" -> "Darstellung"
        "Spanish" -> "Apariencia"
        else -> "Appearance"
    }

    SettingsCategoryDialog(
        icon = Icons.Default.Palette,
        title = title,
        onDismiss = onDismiss
    ) {
        // Theme section
        ThemeSection(
            selectedLanguage = selectedLanguage,
            isDarkMode = isDarkMode,
            onDarkModeChanged = onDarkModeChanged
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // Color management buttons
        ColorManagementSection(
            context = context,
            onResetColors = onResetColors,
            onShowColorSchemeDialog = onShowColorSchemeDialog
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // Color pickers
        ColorPickersSection(
            context = context,
            fontColor = fontColor,
            backgroundColor = backgroundColor,
            gridColor = gridColor,
            insulinColumnColor = insulinColumnColor,
            onFontColorChanged = onFontColorChanged,
            onBackgroundColorChanged = onBackgroundColorChanged,
            onGridColorChanged = onGridColorChanged,
            onInsulinColumnColorChanged = onInsulinColumnColorChanged
        )
    }
}

@Composable
private fun ThemeSection(
    selectedLanguage: String,
    isDarkMode: Boolean,
    onDarkModeChanged: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.dark_mode),
            style = MaterialTheme.typography.bodyMedium
        )
        Switch(
            checked = isDarkMode,
            onCheckedChange = onDarkModeChanged
        )
    }
}

@Composable
private fun ColorManagementSection(
    context: Context,
    onResetColors: () -> Unit,
    onShowColorSchemeDialog: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onResetColors,
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            ExpandableText(
                text = stringResource(R.string.reset_to_defaults),
                style = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                maxLines = 2
            )
        }

        Button(
            onClick = onShowColorSchemeDialog,
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            ExpandableText(
                text = stringResource(R.string.load_color_schemes),
                style = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                maxLines = 2
            )
        }

        Button(
            onClick = onShowColorSchemeDialog,
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            ExpandableText(
                text = stringResource(R.string.save_current_colors_scheme),
                style = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                maxLines = 2
            )
        }
    }
}

@Composable
private fun ColorPickersSection(
    context: Context,
    fontColor: Color,
    backgroundColor: Color,
    gridColor: Color,
    insulinColumnColor: Color,
    onFontColorChanged: (Color) -> Unit,
    onBackgroundColorChanged: (Color) -> Unit,
    onGridColorChanged: (Color) -> Unit,
    onInsulinColumnColorChanged: (Color) -> Unit
) {
    ColorPickerSection(
        title = stringResource(R.string.font_color),
        color = fontColor,
        onColorChange = { newColor ->
            onFontColorChanged(newColor)
            // Settings save is handled by the callback in MainActivity
        },
        onDeleteModeChange = { },
        category = "font"
    )

    ColorPickerSection(
        title = stringResource(R.string.background_color),
        color = backgroundColor,
        onColorChange = { newColor ->
            onBackgroundColorChanged(newColor)
        },
        onDeleteModeChange = { },
        category = "background"
    )

    ColorPickerSection(
        title = stringResource(R.string.grid_color),
        color = gridColor,
        onColorChange = { newColor ->
            onGridColorChanged(newColor)
        },
        onDeleteModeChange = { },
        category = "grid"
    )

    ColorPickerSection(
        title = stringResource(R.string.insulin_column_color),
        color = insulinColumnColor,
        onColorChange = { newColor ->
            onInsulinColumnColorChanged(newColor)
        },
        onDeleteModeChange = { },
        category = "insulin"
    )
}
