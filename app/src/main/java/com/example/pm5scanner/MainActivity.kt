package com.example.pm5scanner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Notification permission granted/denied handled by system
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val devices by viewModel.devices.collectAsStateWithLifecycle()
                    val antServicesMissing by viewModel.antServicesMissing.collectAsStateWithLifecycle()

                    NavHost(navController = navController, startDestination = "scanner") {
                        composable("scanner") {
                            ScannerScreen(
                                devices = devices,
                                antServicesMissing = antServicesMissing,
                                onNavigateToDetails = { deviceNumber ->
                                    if (deviceNumber != null) {
                                        navController.navigate("details/$deviceNumber") {
                                            launchSingleTop = true
                                        }
                                    } else {
                                        navController.navigate("details_empty") {
                                            launchSingleTop = true
                                        }
                                    }
                                },
                                onNavigateToAbout = {
                                    navController.navigate("about") {
                                        launchSingleTop = true
                                    }
                                },
                                onDeviceClick = { deviceNumber ->
                                    navController.navigate("details/$deviceNumber")
                                },
                                onResetConnection = { deviceNumber ->
                                    viewModel.resetDeviceConnection(deviceNumber)
                                },
                                onRefreshList = {
                                    viewModel.clearDeadDevices()
                                }
                            )
                        }
                        composable(
                            "details/{deviceNumber}",
                            arguments = listOf(navArgument("deviceNumber") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val deviceNumber = backStackEntry.arguments?.getInt("deviceNumber")
                            val device = devices.find { it.deviceNumber == deviceNumber }
                            DeviceDetailScreen(
                                device = device,
                                onNavigateToScanner = {
                                    navController.navigate("scanner") {
                                        popUpTo("scanner") { inclusive = true }
                                    }
                                },
                                onNavigateToAbout = {
                                    navController.navigate("about") {
                                        launchSingleTop = true
                                    }
                                },
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable("details_empty") {
                            DeviceDetailScreen(
                                device = null,
                                onNavigateToScanner = {
                                    navController.navigate("scanner") {
                                        popUpTo("scanner") { inclusive = true }
                                    }
                                },
                                onNavigateToAbout = {
                                    navController.navigate("about") {
                                        launchSingleTop = true
                                    }
                                },
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable("about") {
                            AboutScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
