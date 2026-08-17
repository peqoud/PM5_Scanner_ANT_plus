package com.example.pm5scanner

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailScreen(
    device: ANT_Device?,
    onNavigateToScanner: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                 title = { Text("ANT+ Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Scanner View") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onNavigateToScanner()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Detailed view") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("About") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onNavigateToAbout()
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        if (device == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    text = "No device selected. Please select a device from the Scanner View.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailRow(label = "Device Number (ANT ID)", value = device.deviceNumber.toString())
                DetailRow(label = "Serial Number", value = device.serialNumber)
                DetailRow(label = "Status", value = device.status)
                DetailRow(label = "Device Type", value = device.deviceType)
                DetailRow(label = "FE State", value = device.feState)
                DetailRow(label = "Battery Level", value = if (device.batteryLevel >= 0) "${device.batteryLevel}%" else "Unknown")
                DetailRow(label = "Total Distance", value = "${device.totalDistance} m")
                DetailRow(label = "Speed", value = device.speed)
                DetailRow(label = "Resistance Level", value = if (device.resistanceLevel >= 0) device.resistanceLevel.toString() else "N/A")
                DetailRow(label = "Calories", value = "${device.calories} kcal")
                DetailRow(label = "Rower Strokes", value = device.rowerStrokes.toString())
                DetailRow(label = "Rower Cadence", value = "${device.rowerCadence} spm")
                DetailRow(label = "Rower Power", value = "${device.rowerPower} W")
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                Text("Additional Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                DetailRow(label = "Hardware Version", value = device.hardwareVersion.ifEmpty { "N/A" })
                DetailRow(label = "Software Version", value = device.softwareVersion.ifEmpty { "N/A" })
                DetailRow(label = "Model Number", value = device.modelNumber.ifEmpty { "N/A" })
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value)
    }
}
