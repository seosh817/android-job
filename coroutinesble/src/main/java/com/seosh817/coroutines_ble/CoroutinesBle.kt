package com.seosh817.coroutines_ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import com.seosh817.coroutines_ble.logger.Logger
import com.seosh817.coroutines_ble.scanner.CrBleScanner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch


@SuppressLint("MissingPermission")
class CoroutinesBle(private val context: Context) {

    private var bluetoothManager: BluetoothManager? = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        bluetoothManager?.adapter
    }

    private var bleScanner: CrBleScanner? = null

    private val scanScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val adapterStateBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: android.content.Intent?) {
            val action = intent?.action ?: return
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                scanScope.launch {
                    handleScannerState(state)
                    // TODO: disconnect all devices
                }
            }
        }
    }

    private val isBleSupported: Boolean
        get() {
            if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                return true
            }
            Logger.e("BLE not supported")
            return false
        }

    private val isBluetoothEnabled: Boolean
        get() {
            if (bluetoothAdapter?.isEnabled == true) {
                return true
            }
            Logger.e("bluetoothAdapter: ${bluetoothAdapter}, isEnabled: ${bluetoothAdapter?.isEnabled}, Bluetooth disabled")
            return false
        }

    init {
        bleScanner = CrBleScanner.from(bluetoothAdapter)
        context.registerReceiver(adapterStateBroadcastReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
    }

    suspend fun startScan(
        scanFilters: List<ScanFilter> = emptyList(),
        scanSettings: ScanSettings = ScanSettings.Builder().build()
    ): Flow<List<BleScanResult>> {
        try {
            if (!isBleSupported) {
                throw IllegalStateException("BLE not supported")
            }
            if (!isBluetoothEnabled) {
                throw IllegalStateException("Bluetooth disabled")
            }

            Logger.d("startScan: $scanFilters, $scanSettings")

            return bleScanner!!
                .startScan(scanFilters, scanSettings)
                .scan(emptyList()) { acc, value ->
                    if (!acc.map { it.device.address }.contains(value.device.address)) {
                        acc + value
                    } else {
                        acc
                    }
                }
        } catch (e: Exception) {
            Logger.e("startScan error $e")
            throw e
        }
    }

    private fun handleScannerState(state: Int) {
        when (state) {
            BluetoothAdapter.STATE_TURNING_OFF, BluetoothAdapter.STATE_OFF -> {
                if (bleScanner != null) {
                    bleScanner?.stopScan()
                }
            }
        }
    }

    fun close() {
        context.unregisterReceiver(adapterStateBroadcastReceiver)
        bleScanner = null
    }

    companion object {

        fun from(context: Context): CoroutinesBle {
            return CoroutinesBle(context)
        }
    }
}
