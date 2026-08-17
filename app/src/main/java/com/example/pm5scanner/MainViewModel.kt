package com.example.pm5scanner

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.StateFlow

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ANTScannerRepository(application.applicationContext)

    val devices: StateFlow<List<ANT_Device>> = repository.deviceListFlow
    val antServicesMissing: StateFlow<Boolean> = repository.antServicesMissing

    init {
        repository.startScanning()
    }

    fun resetDeviceConnection(deviceNumber: Int) {
        repository.resetDeviceConnection(deviceNumber)
     }

    fun clearDeadDevices() {
        repository.clearDeadDevices()
    }

    override fun onCleared() {
        super.onCleared()
        repository.stopScanning()
    }
}
