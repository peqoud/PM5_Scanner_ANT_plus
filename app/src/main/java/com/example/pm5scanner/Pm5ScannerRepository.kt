package com.example.pm5scanner

import android.content.Context
import android.util.Log
import com.dsi.ant.plugins.antplus.pcc.AntPlusFitnessEquipmentPcc
import com.dsi.ant.plugins.antplus.pcc.defines.DeviceState
import com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult
import com.dsi.ant.plugins.antplus.pccbase.AsyncScanController
import com.dsi.ant.plugins.antplus.pccbase.AsyncScanController.AsyncScanResultDeviceInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class Pm5ScannerRepository(private val context: Context) {
    private val _devices = MutableStateFlow<Map<Int, Pm5Device>>(emptyMap())
    private val _deviceListFlow = MutableStateFlow<List<Pm5Device>>(emptyList())
    val deviceListFlow = _deviceListFlow.asStateFlow()

    private var scanController: AsyncScanController<AntPlusFitnessEquipmentPcc>? = null
    private val connectedPccs = mutableMapOf<Int, AntPlusFitnessEquipmentPcc>()

    fun startScanning() {
        if (scanController != null) return

        Log.d("PM5Scanner", "Starting ANT+ scan...")
        scanController = AntPlusFitnessEquipmentPcc.requestAsyncScanController(
            context,
            0, // Search all devices
            object : AsyncScanController.IAsyncScanResultReceiver {
                override fun onSearchStopped(reason: RequestAccessResult) {
                    Log.d("PM5Scanner", "Search stopped: $reason")
                }

                override fun onSearchResult(deviceInfo: AsyncScanResultDeviceInfo) {
                    val deviceNumber = deviceInfo.antDeviceNumber
                    Log.d("PM5Scanner", "Found device: $deviceNumber")
                    
                    updateDevice(deviceNumber) {
                        it.copy(
                            serialNumber = deviceInfo.deviceDisplayName ?: "Unknown",
                            status = "Discovered"
                        )
                    }

                    // Request access to connect and get real-time data
                    connectToDevice(deviceInfo)
                }
            }
        )
    }

    private fun connectToDevice(deviceInfo: AsyncScanResultDeviceInfo) {
        if (connectedPccs.containsKey(deviceInfo.antDeviceNumber)) return

        AntPlusFitnessEquipmentPcc.requestAccess(
            context,
            deviceInfo.antDeviceNumber,
            0,
            { pcc, resultCode, initialDeviceState ->
                if (resultCode == RequestAccessResult.SUCCESS) {
                    connectedPccs[deviceInfo.antDeviceNumber] = pcc
                    subscribeToDeviceEvents(pcc, deviceInfo.antDeviceNumber)
                }
            },
            { deviceState ->
                updateDevice(deviceInfo.antDeviceNumber) {
                    it.copy(status = deviceState.toString())
                }
            }
        )
    }

    private fun subscribeToDeviceEvents(pcc: AntPlusFitnessEquipmentPcc, deviceNumber: Int) {
        pcc.subscribeGeneralFitnessEquipmentDataEvent { _, _, _, _, _, _, _, totalDistance, _, _, _ ->
            updateDevice(deviceNumber) {
                it.copy(totalDistance = totalDistance)
            }
        }

        pcc.subscribeCommonDataPage82BatteryStatusEvent { _, _, _, _, _, _, _, batteryVoltage, batteryStatus, _, _, _ ->
            updateDevice(deviceNumber) {
                // Simplified battery percentage based on voltage or status if needed.
                // Assuming status or voltage logic can be mapped here. 
                // For now, we'll just store a representation.
                it.copy(batteryLevel = (batteryVoltage.toInt() * 10)) // placeholder logic
            }
        }

        pcc.subscribeManufacturerAndSerialEvent { _, _, _, _, serialNumber ->
            updateDevice(deviceNumber) {
                it.copy(serialNumber = serialNumber.toString())
            }
        }
    }

    private fun updateDevice(deviceNumber: Int, updateBlock: (Pm5Device) -> Pm5Device) {
        _devices.update { currentMap ->
            val existingDevice = currentMap[deviceNumber] ?: Pm5Device(deviceNumber = deviceNumber)
            val updatedDevice = updateBlock(existingDevice)
            val newMap = currentMap + (deviceNumber to updatedDevice)
            _deviceListFlow.value = newMap.values.toList()
            newMap
        }
    }

    fun stopScanning() {
        scanController?.closeScanController()
        scanController = null
        connectedPccs.values.forEach { it.releaseAccess() }
        connectedPccs.clear()
    }
}
