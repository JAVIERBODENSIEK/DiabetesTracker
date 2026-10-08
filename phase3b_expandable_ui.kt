// Expandable Auto-Backup Settings UI Section
// This code should replace the simple auto-backup toggle

                // Auto-backup on exit toggle with expandable settings
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Main toggle row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                autoBackupOnExit = !autoBackupOnExit
                                SettingsManager.saveAutoBackupOnExit(context, autoBackupOnExit)
                            }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when (selectedLanguage) {
                                    "German" -> "Auto-Backup beim Beenden"
                                    "Spanish" -> "Copia de seguridad automática al salir"
                                    else -> "Auto-Backup on Exit"
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = when (selectedLanguage) {
                                    "German" -> "Erstellt automatisch ein Backup im Downloads-Ordner beim Schließen der App"
                                    "Spanish" -> "Crea automáticamente una copia de seguridad en la carpeta Descargas al cerrar la aplicación"
                                    else -> "Automatically creates a backup in Downloads folder when closing the app"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = autoBackupOnExit,
                                onCheckedChange = { 
                                    autoBackupOnExit = it
                                    SettingsManager.saveAutoBackupOnExit(context, autoBackupOnExit)
                                }
                            )
                            IconButton(
                                onClick = { showAutoBackupSettings = !showAutoBackupSettings },
                                enabled = autoBackupOnExit
                            ) {
                                Icon(
                                    imageVector = if (showAutoBackupSettings) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = "Expand settings"
                                )
                            }
                        }
                    }
                    
                    // Expandable advanced settings
                    AnimatedVisibility(visible = showAutoBackupSettings && autoBackupOnExit) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Backup Frequency
                                Text(
                                    text = when (selectedLanguage) {
                                        "German" -> "Backup-Häufigkeit"
                                        "Spanish" -> "Frecuencia de copia de seguridad"
                                        else -> "Backup Frequency"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("Daily", "Weekly", "Monthly").forEach { freq ->
                                        val label = when (freq) {
                                            "Daily" -> when (selectedLanguage) {
                                                "German" -> "Täglich"
                                                "Spanish" -> "Diario"
                                                else -> "Daily"
                                            }
                                            "Weekly" -> when (selectedLanguage) {
                                                "German" -> "Wöchentlich"
                                                "Spanish" -> "Semanal"
                                                else -> "Weekly"
                                            }
                                            "Monthly" -> when (selectedLanguage) {
                                                "German" -> "Monatlich"
                                                "Spanish" -> "Mensual"
                                                else -> "Monthly"
                                            }
                                            else -> freq
                                        }
                                        
                                        FilterChip(
                                            selected = autoBackupFrequency == freq,
                                            onClick = {
                                                autoBackupFrequency = freq
                                                SettingsManager.saveAutoBackupFrequency(context, freq)
                                            },
                                            label = { Text(label) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Max Backup Count
                                Text(
                                    text = when (selectedLanguage) {
                                        "German" -> "Maximale Anzahl Backups: $autoBackupMaxCount"
                                        "Spanish" -> "Número máximo de copias: $autoBackupMaxCount"
                                        else -> "Max Backup Count: $autoBackupMaxCount"
                                    },
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = when (selectedLanguage) {
                                        "German" -> "Älteste Backups werden automatisch gelöscht"
                                        "Spanish" -> "Las copias más antiguas se eliminan automáticamente"
                                        else -> "Oldest backups are automatically deleted"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                Slider(
                                    value = autoBackupMaxCount.toFloat(),
                                    onValueChange = { 
                                        autoBackupMaxCount = it.toInt()
                                        SettingsManager.saveAutoBackupMaxCount(context, autoBackupMaxCount)
                                    },
                                    valueRange = 1f..5f,
                                    steps = 3,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Backup Path Display
                                Text(
                                    text = when (selectedLanguage) {
                                        "German" -> "Speicherort"
                                        "Spanish" -> "Ubicación de guardado"
                                        else -> "Save Location"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                
                                Text(
                                    text = autoBackupPath,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(8.dp)
                                )
                                
                                // Note about path (Downloads is default and recommended)
                                Text(
                                    text = when (selectedLanguage) {
                                        "German" -> "📁 Standard: Downloads-Ordner (empfohlen)"
                                        "Spanish" -> "📁 Predeterminado: Carpeta Descargas (recomendado)"
                                        else -> "📁 Default: Downloads folder (recommended)"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
