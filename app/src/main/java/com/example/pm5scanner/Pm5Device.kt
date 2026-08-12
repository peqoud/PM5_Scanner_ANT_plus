package com.example.pm5scanner

data class Pm5Device(
    val deviceNumber: Int,
    val serialNumber: String = "Unknown",
    val batteryLevel: Int = -1,
    val status: String = "Connecting...",
    val totalDistance: Long = 0,
    val capabilities: String = "",
    val hardwareVersion: String = "",
    val softwareVersion: String = "",
    val modelNumber: String = ""
)
