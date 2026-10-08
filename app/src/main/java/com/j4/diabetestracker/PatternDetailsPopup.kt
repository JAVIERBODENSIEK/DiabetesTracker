package com.j4.diabetestracker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlin.math.roundToInt

/**
 * Popup dialog showing detailed pattern information for a specific food
 */
@Composable
fun PatternDetailsPopup(
    foodName: String,
    patterns: List<DetectedPattern>,
    selectedLanguage: String,
    onDismiss: () -> Unit,
    onNavigateToDate: (String) -> Unit = {}
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            when (selectedLanguage) {
                                "German" -> "Musterdetails"
                                "Spanish" -> "Detalles del Patrón"
                                else -> "Pattern Details"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = when (selectedLanguage) {
                                "German" -> "Schließen"
                                "Spanish" -> "Cerrar"
                                else -> "Close"
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Food name
                Text(
                    text = when (selectedLanguage) {
                        "German" -> "Lebensmittel: $foodName"
                        "Spanish" -> "Alimento: $foodName"
                        else -> "Food: $foodName"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Scrollable pattern list
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (patterns.isEmpty()) {
                        Text(
                            when (selectedLanguage) {
                                "German" -> "Keine Muster gefunden"
                                "Spanish" -> "No se encontraron patrones"
                                else -> "No patterns found"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        patterns.sortedByDescending { it.confidenceScore }.forEach { pattern ->
                            PatternDetailCard(
                                pattern = pattern,
                                selectedLanguage = selectedLanguage,
                                onNavigateToDate = onNavigateToDate
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Disclaimer
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        when (selectedLanguage) {
                            "German" -> "ℹ️ Diese Muster basieren auf statistischer Analyse. Konsultieren Sie Ihren Arzt für medizinische Beratung."
                            "Spanish" -> "ℹ️ Estos patrones se basan en análisis estadístico. Consulte a su médico para asesoramiento médico."
                            else -> "ℹ️ These patterns are based on statistical analysis. Consult your doctor for medical advice."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PatternDetailCard(
    pattern: DetectedPattern,
    selectedLanguage: String,
    onNavigateToDate: (String) -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showAllOccurrences by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                pattern.confidenceScore >= 0.8f -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                pattern.confidenceScore >= 0.6f -> Color(0xFFFFE0B2).copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Marker name and "After" info
            Text(
                text = translateMarkerName(pattern.markerName, selectedLanguage),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                when (selectedLanguage) {
                    "German" -> "Nach: ${translateTimeOfDay(pattern.correlatedItem, selectedLanguage)}"
                    "Spanish" -> "Después de: ${translateTimeOfDay(pattern.correlatedItem, selectedLanguage)}"
                    else -> "After: ${translateTimeOfDay(pattern.correlatedItem, selectedLanguage)}"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Confidence badge and occurrences
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ConfidenceBadge(pattern.confidenceScore, selectedLanguage)
                
                Text(
                    when (selectedLanguage) {
                        "German" -> "${pattern.occurrences} Vorkommen"
                        "Spanish" -> "${pattern.occurrences} ocurrencia${if (pattern.occurrences != 1) "s" else ""}"
                        else -> "${pattern.occurrences} occurrence${if (pattern.occurrences != 1) "s" else ""}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Suggestion
            Text(
                translateSuggestion(pattern.suggestion, selectedLanguage),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Expandable details section
            if (pattern.details.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                
                TextButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (isExpanded) 
                            when (selectedLanguage) {
                                "German" -> "Details ausblenden"
                                "Spanish" -> "Ocultar detalles"
                                else -> "Hide Details"
                            }
                        else 
                            when (selectedLanguage) {
                                "German" -> "Details anzeigen"
                                "Spanish" -> "Mostrar detalles"
                                else -> "Show Details"
                            }
                    )
                    Icon(
                        if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                if (isExpanded) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            when (selectedLanguage) {
                                "German" -> "Vorkommen:"
                                "Spanish" -> "Ocurrencias:"
                                else -> "Occurrences:"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        val displayedOccurrences = if (showAllOccurrences) pattern.details else pattern.details.take(5)
                        
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = if (showAllOccurrences) 300.dp else 1000.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            displayedOccurrences.forEach { occurrence ->
                                OccurrenceItem(
                                    occurrence = occurrence,
                                    selectedLanguage = selectedLanguage,
                                    onClick = {
                                        onNavigateToDate("${occurrence.date}|${occurrence.timeOfDay}")
                                    }
                                )
                            }
                        }
                        
                        if (pattern.details.size > 5 && !showAllOccurrences) {
                            TextButton(
                                onClick = { showAllOccurrences = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    when (selectedLanguage) {
                                        "German" -> "Alle ${pattern.details.size} anzeigen"
                                        "Spanish" -> "Mostrar todos (${pattern.details.size})"
                                        else -> "Show All (${pattern.details.size})"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        
                        if (showAllOccurrences) {
                            TextButton(
                                onClick = { showAllOccurrences = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    when (selectedLanguage) {
                                        "German" -> "Weniger anzeigen"
                                        "Spanish" -> "Mostrar menos"
                                        else -> "Show Less"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    Icons.Default.KeyboardArrowUp,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

