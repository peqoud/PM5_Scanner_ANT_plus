package com.example.pm5scanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                                antServicesMissing = antServicesMissing
                            ) { deviceNumber ->
                                navController.navigate("details/$deviceNumber")
                            }
                        }
                        composable(
                            "details/{deviceNumber}",
                            arguments = listOf(navArgument("deviceNumber") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val deviceNumber = backStackEntry.arguments?.getInt("deviceNumber")
                            val device = devices.find { it.deviceNumber == deviceNumber }
                            if (device != null) {
                                DeviceDetailScreen(device = device) {
                                    navController.popBackStack()
                                }
                            } else {
                                Text("Device not found")
                            }
                        }
                    }
                }
            }
        }
    }
}
