package com.example.pm5scanner

data class ANT_Device(
    val deviceNumber: Int,
    val serialNumber: String = "Unknown",
    val batteryLevel: Int = -1,
    val status: String = "Connecting...",
    val totalDistance: Long = 0,
    val capabilities: String = "",
    val hardwareVersion: String = "",
    val softwareVersion: String = "",
    val modelNumber: String = "",
    val deviceType: String = "Unknown",
    val feState: String = "Unknown",
    val speed: String = "0.0 m/s",
    val resistanceLevel: Int = -1,
    val calories: Long = 0,
    val rowerStrokes: Long = 0,
    val rowerCadence: Int = 0,
    val rowerPower: Int = 0
)
