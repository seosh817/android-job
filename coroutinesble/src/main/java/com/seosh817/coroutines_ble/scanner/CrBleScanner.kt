package com.seosh817.coroutines_ble.scanner

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import androidx.annotation.RequiresPermission
import com.seosh817.coroutines_ble.BleScanResult
import com.seosh817.coroutines_ble.toBleScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn

@SuppressLint("MissingPermission")
internal class CrBleScanner private constructor(private val bluetoothAdapter: BluetoothAdapter?) {

    private var scanCallback: ScanCallback? = null

    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT])
    suspend fun startScan(scanFilters: List<ScanFilter>, scanSettings: ScanSettings): Flow<BleScanResult> = callbackFlow {
        requireNotNull(bluetoothAdapter?.bluetoothLeScanner) { "bluetoothLeScanner is null. Is the Adapter on?" }
        scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                trySend(result.toBleScanResult())
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                results?.map { it.toBleScanResult() }?.forEach { trySend(it) }
            }

            override fun onScanFailed(errorCode: Int) {
                close(RuntimeException("Scan failed with error code $errorCode"))
            }
        }

        bluetoothAdapter?.bluetoothLeScanner?.startScan(scanFilters, scanSettings, scanCallback)
        awaitClose { stopScan() }
    }.flowOn(Dispatchers.IO)

    fun stopScan() {
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        scanCallback = null
    }

    companion object {

        @Volatile
        private var mInstance: CrBleScanner? = null

        @JvmStatic
        fun from(bluetoothAdapter: BluetoothAdapter?): CrBleScanner =
            mInstance ?: synchronized(this) {
                CrBleScanner(bluetoothAdapter).also { mInstance = it }
            }
    }
}
