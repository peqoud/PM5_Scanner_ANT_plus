package com.example.pm5scanner

import android.content.Context
import android.util.Log
import com.dsi.ant.plugins.antplus.pcc.AntPlusFitnessEquipmentPcc
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

    private val _antServicesMissing = MutableStateFlow(false)
    val antServicesMissing = _antServicesMissing.asStateFlow()

    private var scanController: AsyncScanController<AntPlusFitnessEquipmentPcc>? = null
    private val connectedPccs = mutableMapOf<Int, AntPlusFitnessEquipmentPcc>()

    fun startScanning() {
        if (scanController != null) return

        Log.d("PM5Scanner", "Starting ANT+ scan...")
        scanController = AntPlusFitnessEquipmentPcc.requestNewOpenAccess(
            context,
            0,
            object : AsyncScanController.IAsyncScanResultReceiver {
                override fun onSearchStopped(reason: RequestAccessResult) {
                    Log.d("PM5Scanner", "Search stopped: $reason")
                    if (reason == RequestAccessResult.DEPENDENCY_NOT_INSTALLED) {
                        _antServicesMissing.value = true
                    }
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

                    connectToDevice(deviceInfo)
                }
            },
            object : AntPlusFitnessEquipmentPcc.IFitnessEquipmentStateReceiver {
                override fun onNewFitnessEquipmentState(
                    estTimestamp: Long,
                    eventFlags: java.util.EnumSet<com.dsi.ant.plugins.antplus.pcc.defines.EventFlag>,
                    equipmentType: AntPlusFitnessEquipmentPcc.EquipmentType,
                    equipmentState: AntPlusFitnessEquipmentPcc.EquipmentState
                ) {
                    Log.d("PM5Scanner", "State received: $equipmentType, $equipmentState")
                }
            }
        )
    }

    private fun connectToDevice(deviceInfo: AsyncScanResultDeviceInfo) {
        if (connectedPccs.containsKey(deviceInfo.antDeviceNumber)) return

        AntPlusFitnessEquipmentPcc.requestNewOpenAccess(
            context,
            deviceInfo.antDeviceNumber,
            0,
            object : com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc.IPluginAccessResultReceiver<AntPlusFitnessEquipmentPcc> {
                override fun onResultReceived(
                    result: AntPlusFitnessEquipmentPcc?,
                    resultCode: RequestAccessResult,
                    initialDeviceState: com.dsi.ant.plugins.antplus.pcc.defines.DeviceState
                ) {
                    if (resultCode == RequestAccessResult.SUCCESS && result != null) {
                        connectedPccs[deviceInfo.antDeviceNumber] = result
                        subscribeToDeviceEvents(result, deviceInfo.antDeviceNumber)
                    }
                }
            },
            object : com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc.IDeviceStateChangeReceiver {
                override fun onDeviceStateChange(deviceState: com.dsi.ant.plugins.antplus.pcc.defines.DeviceState) {
                    updateDevice(deviceInfo.antDeviceNumber) {
                        it.copy(status = deviceState.toString())
                    }
                }
            },
            object : AntPlusFitnessEquipmentPcc.IFitnessEquipmentStateReceiver {
                override fun onNewFitnessEquipmentState(
                    estTimestamp: Long,
                    eventFlags: java.util.EnumSet<com.dsi.ant.plugins.antplus.pcc.defines.EventFlag>,
                    equipmentType: AntPlusFitnessEquipmentPcc.EquipmentType,
                    equipmentState: AntPlusFitnessEquipmentPcc.EquipmentState
                ) {
                    Log.d("PM5Scanner", "Device internal state: $equipmentState, type: $equipmentType")
                    updateDevice(deviceInfo.antDeviceNumber) {
                        it.copy(
                            deviceType = equipmentType.toString(),
                            feState = equipmentState.toString()
                        )
                    }
                }
            }
        )
    }

    private fun subscribeToDeviceEvents(pcc: AntPlusFitnessEquipmentPcc, deviceNumber: Int) {
        pcc.subscribeGeneralFitnessEquipmentDataEvent(
            object : AntPlusFitnessEquipmentPcc.IGeneralFitnessEquipmentDataReceiver {
                override fun onNewGeneralFitnessEquipmentData(
                    estTimestamp: Long,
                    eventFlags: java.util.EnumSet<com.dsi.ant.plugins.antplus.pcc.defines.EventFlag>,
                    cumulativeDistance: java.math.BigDecimal,
                    instantaneousSpeed: Long,
                    instantaneousPower: java.math.BigDecimal,
                    expHrt: Boolean,
                    instantaneousHeartRate: Int,
                    heartRateDataSource: AntPlusFitnessEquipmentPcc.HeartRateDataSource
                ) {
                    val speedDisplay = if (instantaneousSpeed >= 0) "${instantaneousSpeed / 1000.0} m/s" else "0.0 m/s"
                    updateDevice(deviceNumber) {
                        it.copy(
                            totalDistance = cumulativeDistance.toLong(),
                            speed = speedDisplay
                        )
                    }
                }
            }
        )

        pcc.subscribeGeneralSettingsEvent(
            object : AntPlusFitnessEquipmentPcc.IGeneralSettingsReceiver {
                override fun onNewGeneralSettings(
                    estTimestamp: Long,
                    eventFlags: java.util.EnumSet<com.dsi.ant.plugins.antplus.pcc.defines.EventFlag>,
                    cycleLength: java.math.BigDecimal,
                    inclinePercentage: java.math.BigDecimal,
                    resistanceLevel: Int
                ) {
                    updateDevice(deviceNumber) {
                        it.copy(resistanceLevel = resistanceLevel)
                    }
                }
            }
        )

        pcc.subscribeGeneralMetabolicDataEvent(
            object : AntPlusFitnessEquipmentPcc.IGeneralMetabolicDataReceiver {
                override fun onNewGeneralMetabolicData(
                    estTimestamp: Long,
                    eventFlags: java.util.EnumSet<com.dsi.ant.plugins.antplus.pcc.defines.EventFlag>,
                    instantaneousMetabolicEquivalents: java.math.BigDecimal,
                    instantaneousCaloricBurn: java.math.BigDecimal,
                    cumulativeCalories: Long
                ) {
                    updateDevice(deviceNumber) {
                        it.copy(calories = cumulativeCalories)
                    }
                }
            }
        )

        pcc.rowerMethods?.subscribeRowerDataEvent(
            object : AntPlusFitnessEquipmentPcc.IRowerDataReceiver {
                override fun onNewRowerData(
                    estTimestamp: Long,
                    eventFlags: java.util.EnumSet<com.dsi.ant.plugins.antplus.pcc.defines.EventFlag>,
                    cumulativeStrokes: Long,
                    instantaneousCadence: Int,
                    instantaneousPower: Int
                ) {
                    updateDevice(deviceNumber) {
                        it.copy(
                            rowerStrokes = cumulativeStrokes,
                            rowerCadence = instantaneousCadence,
                            rowerPower = instantaneousPower
                        )
                    }
                }
            }
        )

        pcc.subscribeBatteryStatusEvent(
            object : com.dsi.ant.plugins.antplus.pccbase.AntPlusCommonPcc.IBatteryStatusReceiver {
                override fun onNewBatteryStatus(
                    estTimestamp: Long,
                    eventFlags: java.util.EnumSet<com.dsi.ant.plugins.antplus.pcc.defines.EventFlag>,
                    cumulativeOperatingTime: Long,
                    batteryVoltage: java.math.BigDecimal,
                    batteryStatus: com.dsi.ant.plugins.antplus.pcc.defines.BatteryStatus,
                    cumulativeOperatingTimeRes: Int,
                    batteryIdentifier: Int,
                    numberOfBatteries: Int
                ) {
                    updateDevice(deviceNumber) {
                        it.copy(batteryLevel = batteryVoltage.toInt() * 10)
                    }
                }
            }
        )

        pcc.subscribeProductInformationEvent(
            object : com.dsi.ant.plugins.antplus.pccbase.AntPlusCommonPcc.IProductInformationReceiver {
                override fun onNewProductInformation(
                    estTimestamp: Long,
                    eventFlags: java.util.EnumSet<com.dsi.ant.plugins.antplus.pcc.defines.EventFlag>,
                    hardwareVersion: Int,
                    softwareVersion: Int,
                    serialNumber: Long
                ) {
                    updateDevice(deviceNumber) {
                        it.copy(serialNumber = serialNumber.toString())
                    }
                }
            }
        )

        pcc.subscribeManufacturerIdentificationEvent(
            object : com.dsi.ant.plugins.antplus.pccbase.AntPlusCommonPcc.IManufacturerIdentificationReceiver {
                override fun onNewManufacturerIdentification(
                    estTimestamp: Long,
                    eventFlags: java.util.EnumSet<com.dsi.ant.plugins.antplus.pcc.defines.EventFlag>,
                    hardwareRevision: Int,
                    manufacturerID: Int,
                    modelNumber: Int
                ) {
                    updateDevice(deviceNumber) {
                        it.copy(modelNumber = modelNumber.toString())
                    }
                }
            }
        )
    }

    fun resetDeviceConnection(deviceNumber: Int) {
        Log.d("PM5Scanner", "Resetting connection for device: $deviceNumber")
        connectedPccs.remove(deviceNumber)?.releaseAccess()

        updateDevice(deviceNumber) {
            it.copy(status = "Connecting...")
        }

        AntPlusFitnessEquipmentPcc.requestNewOpenAccess(
            context,
            deviceNumber,
            0,
            object : com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc.IPluginAccessResultReceiver<AntPlusFitnessEquipmentPcc> {
                override fun onResultReceived(
                    result: AntPlusFitnessEquipmentPcc?,
                    resultCode: RequestAccessResult,
                    initialDeviceState: com.dsi.ant.plugins.antplus.pcc.defines.DeviceState
                ) {
                    if (resultCode == RequestAccessResult.SUCCESS && result != null) {
                        connectedPccs[deviceNumber] = result
                        subscribeToDeviceEvents(result, deviceNumber)
                    } else {
                        updateDevice(deviceNumber) {
                            it.copy(status = resultCode.toString())
                        }
                    }
                }
            },
            object : com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc.IDeviceStateChangeReceiver {
                override fun onDeviceStateChange(deviceState: com.dsi.ant.plugins.antplus.pcc.defines.DeviceState) {
                    updateDevice(deviceNumber) {
                        it.copy(status = deviceState.toString())
                    }
                }
            },
            object : AntPlusFitnessEquipmentPcc.IFitnessEquipmentStateReceiver {
                override fun onNewFitnessEquipmentState(
                    estTimestamp: Long,
                    eventFlags: java.util.EnumSet<com.dsi.ant.plugins.antplus.pcc.defines.EventFlag>,
                    equipmentType: AntPlusFitnessEquipmentPcc.EquipmentType,
                    equipmentState: AntPlusFitnessEquipmentPcc.EquipmentState
                ) {
                    Log.d("PM5Scanner", "Device internal state: $equipmentState, type: $equipmentType")
                    updateDevice(deviceNumber) {
                        it.copy(
                            deviceType = equipmentType.toString(),
                            feState = equipmentState.toString()
                        )
                    }
                }
            }
        )
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

    fun clearDeadDevices() {
        _devices.update { currentMap ->
            val deadDeviceNumbers = currentMap.values
                .filter { it.status.equals("DEAD", ignoreCase = true) }
                .map { it.deviceNumber }
            
            deadDeviceNumbers.forEach { devNum ->
                connectedPccs.remove(devNum)?.releaseAccess()
            }

            val newMap = currentMap.filterKeys { it !in deadDeviceNumbers }
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
