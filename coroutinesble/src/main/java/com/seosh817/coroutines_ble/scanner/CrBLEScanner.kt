package com.seosh817.coroutines_ble.scanner

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@SuppressLint("MissingPermission")
internal class CrBLEScanner private constructor(private val bluetoothAdapter: BluetoothAdapter?) {

    init {
        requireNotNull(bluetoothAdapter?.bluetoothLeScanner) { "bluetoothLeScanner is null. Is the Adapter on?" }
    }

    var isScanning = false
        private set

    private val scanResultChannel = Channel<ScanResult>(Channel.CONFLATED)

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            scanResultChannel.trySend(result)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)

        }
    }

    suspend fun scanDevices(scanFilters: List<ScanFilter>, scanSettings: ScanSettings) = callbackFlow {
        isScanning = true
        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                trySend(result.device)
            }

            override fun onScanFailed(errorCode: Int) {
                close(RuntimeException("Scan failed with error code $errorCode"))
                isScanning = false
            }
        }

        bluetoothAdapter?.bluetoothLeScanner?.startScan(scanFilters, scanSettings, callback)

        awaitClose { stopScan() }
    }

    fun stopScan() {
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        isScanning = false
    }

    companion object {

        fun from(bluetoothAdapter: BluetoothAdapter?): CrBLEScanner {
            return CrBLEScanner(bluetoothAdapter)
        }

        suspend fun scanDevices(
            bluetoothAdapter: BluetoothAdapter?,
            scanFilters: List<ScanFilter>,
            scanSettings: ScanSettings
        ): Flow<BluetoothDevice> {
            return from(bluetoothAdapter)
                .scanDevices(scanFilters, scanSettings)
        }
    }
}
