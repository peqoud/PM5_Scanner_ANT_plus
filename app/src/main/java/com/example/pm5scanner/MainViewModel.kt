package com.example.pm5scanner

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val serviceRepository = MutableStateFlow<ANTScannerRepository?>(null)

    val devices: StateFlow<List<ANT_Device>> = serviceRepository
        .flatMapLatest { repo -> repo?.deviceListFlow ?: flowOf(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val antServicesMissing: StateFlow<Boolean> = serviceRepository
        .flatMapLatest { repo -> repo?.antServicesMissing ?: flowOf(false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as? ANTBackgroundService.LocalBinder
            serviceRepository.value = binder?.getService()?.repository
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            serviceRepository.value = null
        }
    }

    init {
        startAndBindService()
    }

    private fun startAndBindService() {
        val app = getApplication<Application>()
        val intent = Intent(app, ANTBackgroundService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            app.startForegroundService(intent)
        } else {
            app.startService(intent)
        }

        app.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun resetDeviceConnection(deviceNumber: Int) {
        serviceRepository.value?.resetDeviceConnection(deviceNumber)
    }

    fun clearDeadDevices() {
        serviceRepository.value?.clearDeadDevices()
    }

    fun stopServiceAndExit() {
        val app = getApplication<Application>()
        try {
            app.unbindService(serviceConnection)
        } catch (_: Exception) {
        }
        val stopIntent = Intent(app, ANTBackgroundService::class.java).apply {
            action = ANTBackgroundService.ACTION_STOP_SERVICE
        }
        app.startService(stopIntent)
    }

    override fun onCleared() {
        super.onCleared()
        try {
            getApplication<Application>().unbindService(serviceConnection)
        } catch (_: Exception) {
        }
    }
}
