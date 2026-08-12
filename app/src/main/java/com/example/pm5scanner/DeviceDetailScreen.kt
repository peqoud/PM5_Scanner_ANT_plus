package com.example.pm5scanner

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailScreen(
    device: Pm5Device,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PM5 Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
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
            DetailRow(label = "Battery Level", value = if (device.batteryLevel >= 0) "${device.batteryLevel}%" else "Unknown")
            DetailRow(label = "Total Distance", value = "${device.totalDistance} m")
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            Text("Additional Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            DetailRow(label = "Hardware Version", value = device.hardwareVersion.ifEmpty { "N/A" })
            DetailRow(label = "Software Version", value = device.softwareVersion.ifEmpty { "N/A" })
            DetailRow(label = "Model Number", value = device.modelNumber.ifEmpty { "N/A" })
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
