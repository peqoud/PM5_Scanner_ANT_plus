package com.example.pm5scanner

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.StateFlow

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = Pm5ScannerRepository(application.applicationContext)

    val devices: StateFlow<List<Pm5Device>> = repository.deviceListFlow
    val antServicesMissing: StateFlow<Boolean> = repository.antServicesMissing

    init {
        repository.startScanning()
    }

    override fun onCleared() {
        super.onCleared()
        repository.stopScanning()
    }
}
