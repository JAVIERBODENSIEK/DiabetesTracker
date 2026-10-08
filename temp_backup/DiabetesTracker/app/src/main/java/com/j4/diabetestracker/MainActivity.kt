package com.j4.diabetestracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.j4.diabetestracker.ui.theme.DiabetesTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DiabetesTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DiabetesTrackerApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiabetesTrackerApp() {
    var selectedLanguage by remember { mutableStateOf("English") }
    var showLanguageMenu by remember { mutableStateOf(false) }
    var date by remember { mutableStateOf("") }
    var morningSugar by remember { mutableStateOf("") }
    var afternoonSugar by remember { mutableStateOf("") }
    var eveningSugar by remember { mutableStateOf("") }
    var nightSugar by remember { mutableStateOf("") }
    var insulinMorning by remember { mutableStateOf("") }
    var insulinAfternoon by remember { mutableStateOf("") }
    var insulinEvening by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Diabetes Tracker") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Language Selection
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Select Language",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Box {
                        TextButton(onClick = { showLanguageMenu = true }) {
                            Text(selectedLanguage)
                        }
                        DropdownMenu(
                            expanded = showLanguageMenu,
                            onDismissRequest = { showLanguageMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("English") },
                                onClick = { 
                                    selectedLanguage = "English"
                                    showLanguageMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Spanish") },
                                onClick = { 
                                    selectedLanguage = "Spanish"
                                    showLanguageMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("German") },
                                onClick = { 
                                    selectedLanguage = "German"
                                    showLanguageMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Blood Sugar Measurements
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Blood Sugar Measurements",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date (YYYY-MM-DD)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )

                    OutlinedTextField(
                        value = morningSugar,
                        onValueChange = { morningSugar = it },
                        label = { Text("Morning Sugar (mg/dL)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )

                    OutlinedTextField(
                        value = afternoonSugar,
                        onValueChange = { afternoonSugar = it },
                        label = { Text("Afternoon Sugar (mg/dL)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )

                    OutlinedTextField(
                        value = eveningSugar,
                        onValueChange = { eveningSugar = it },
                        label = { Text("Evening Sugar (mg/dL)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )

                    OutlinedTextField(
                        value = nightSugar,
                        onValueChange = { nightSugar = it },
                        label = { Text("Night Sugar (mg/dL)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }
            }

            // Insulin Dosage
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Insulin Dosage",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = insulinMorning,
                        onValueChange = { insulinMorning = it },
                        label = { Text("Morning Insulin (units)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )

                    OutlinedTextField(
                        value = insulinAfternoon,
                        onValueChange = { insulinAfternoon = it },
                        label = { Text("Afternoon Insulin (units)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )

                    OutlinedTextField(
                        value = insulinEvening,
                        onValueChange = { insulinEvening = it },
                        label = { Text("Evening Insulin (units)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }
            }

            // Save Button
            Button(
                onClick = {
                    // TODO: Implement save functionality
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("Save")
            }
        }
    }
}