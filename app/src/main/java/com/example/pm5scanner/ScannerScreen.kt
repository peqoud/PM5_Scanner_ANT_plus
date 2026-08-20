package com.example.pm5scanner

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    devices: List<ANT_Device>,
    antServicesMissing: Boolean,
    onNavigateToDetails: (Int?) -> Unit,
    onNavigateToAbout: () -> Unit,
    onDeviceClick: (Int) -> Unit,
    onResetConnection: (Int) -> Unit = {},
    onRefreshList: () -> Unit = {},
    onStopServiceAndExit: () -> Unit = {}
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var sortByDistanceAsc by remember { mutableStateOf<Boolean?>(null) }
    var groupByDeviceType by remember { mutableStateOf(false) }

    val processedDevices = remember(devices, sortByDistanceAsc) {
        when (sortByDistanceAsc) {
            true -> devices.sortedBy { it.totalDistance }
            false -> devices.sortedByDescending { it.totalDistance }
            null -> devices
        }
    }

    val groupedDevices = remember(processedDevices, groupByDeviceType) {
        if (groupByDeviceType) {
            processedDevices.groupBy { it.deviceType }
        } else {
            mapOf("" to processedDevices)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (antServicesMissing) "ANT+ Services Missing" else "ANT+ Scanner") },
                navigationIcon = {
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
                                    onNavigateToDetails(devices.firstOrNull()?.deviceNumber)
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
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Stop & Exit App") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onStopServiceAndExit()
                                }
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onRefreshList) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Clear & Refresh"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (antServicesMissing) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = if (antServicesMissing) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        if (antServicesMissing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "ANT+ Radio Service is not installed or missing on this device.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Please install ANT Radio Service, ANT+ Plugins Service, and ANT USB Service from the Play Store to scan for ANT+ rowers.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else if (devices.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Searching for ANT+ devices...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Controls Row for sorting by distance and grouping by deviceType
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = sortByDistanceAsc != null,
                        onClick = {
                            sortByDistanceAsc = when (sortByDistanceAsc) {
                                null -> false // default sort descending (highest distance first)
                                false -> true  // ascending (lowest distance first)
                                true -> null  // disable sorting
                            }
                        },
                        label = {
                            Text(
                                when (sortByDistanceAsc) {
                                    true -> "Distance (Asc \u2191)"
                                    false -> "Distance (Desc \u2193)"
                                    null -> "Sort Distance"
                                }
                            )
                        }
                    )

                    FilterChip(
                        selected = groupByDeviceType,
                        onClick = { groupByDeviceType = !groupByDeviceType },
                        label = { Text("Group by Type") }
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    groupedDevices.forEach { (type, deviceList) ->
                        if (groupByDeviceType) {
                            item(key = "header_$type") {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = MaterialTheme.shapes.small,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp, bottom = 4.dp)
                                ) {
                                    Text(
                                        text = type.ifBlank { "Unknown Type" },
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        items(deviceList, key = { it.deviceNumber }) { device ->
                            DeviceListItem(
                                device = device,
                                showDeviceType = !groupByDeviceType,
                                onClick = {
                                    if (device.status.equals("DEAD", ignoreCase = true)) {
                                        onResetConnection(device.deviceNumber)
                                    } else {
                                        onDeviceClick(device.deviceNumber)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceListItem(
    device: ANT_Device,
    showDeviceType: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = device.serialNumber,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = device.status,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.wrapContentWidth(Alignment.End)
            ) {
                Text(
                    text = "${device.totalDistance} m",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (showDeviceType && device.deviceType.isNotBlank()) {
                    Text(
                        text = device.deviceType,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}
