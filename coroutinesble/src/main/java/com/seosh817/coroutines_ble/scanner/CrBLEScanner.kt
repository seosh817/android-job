package com.seosh817.coroutines_ble.scanner

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart

@SuppressLint("MissingPermission")
internal class CrBLEScanner private constructor(private val bluetoothAdapter: BluetoothAdapter?) {

    private var scanCallback: ScanCallback? = null

    var isScanning = false
        private set

    init {
        requireNotNull(bluetoothAdapter?.bluetoothLeScanner) { "bluetoothLeScanner is null. Is the Adapter on?" }
    }

    suspend fun startScan(scanFilters: List<ScanFilter>, scanSettings: ScanSettings) = callbackFlow {
        scanCallback?.apply {
            object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult) {
                    trySend(result.device)
                }

                override fun onScanFailed(errorCode: Int) {
                    close(RuntimeException("Scan failed with error code $errorCode"))
                    isScanning = false
                }
            }
        }

        bluetoothAdapter?.bluetoothLeScanner?.startScan(scanFilters, scanSettings, scanCallback)

        awaitClose { stopScan() }
    }
        .flowOn(Dispatchers.IO)
        .onStart { isScanning = true }
        .onCompletion { isScanning = false }

    fun stopScan() {
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        scanCallback = null
    }

    companion object {

        @Volatile
        private var mInstance: CrBLEScanner? = null

        @JvmStatic
        fun from(bluetoothAdapter: BluetoothAdapter?): CrBLEScanner =
            mInstance ?: synchronized(this) {
                CrBLEScanner(bluetoothAdapter).also { mInstance = it }
            }
    }
}
