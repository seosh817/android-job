package com.seosh817.coroutines_ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import com.seosh817.coroutines_ble.logger.Logger
import com.seosh817.coroutines_ble.scanner.CrBLEScanner

@SuppressLint("MissingPermission")
class CoroutinesBLE(private val context: Context) {

    private var bluetoothManager: BluetoothManager? = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        bluetoothManager?.adapter
    }

    private var bleScanner: CrBLEScanner? = null

    private val adapterStateBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: android.content.Intent?) {
            val action = intent?.action ?: return
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                handleScannerState(state)
                // TODO: disconnect all devices
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
            if (bluetoothAdapter?.isEnabled == false) {
                return true
            }
            Logger.e("Bluetooth disabled")
            return false
        }

    init {
        bleScanner = CrBLEScanner.from(bluetoothAdapter)
        context.registerReceiver(adapterStateBroadcastReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
    }

    suspend fun startScan(scanFilters: List<ScanFilter> = emptyList(), scanSettings: ScanSettings = ScanSettings.Builder().build()) {
        try {
            if (!isBleSupported || !isBluetoothEnabled) {
                return
            }

            bleScanner?.startScan(scanFilters, scanSettings)
        } catch (e: Exception) {
            Logger.e("startScan error $e")
        }
    }

    fun stopScan() {
        try {
            bleScanner?.stopScan()
        } catch (e: Exception) {
            Logger.e("stopScan error $e")
        }
    }

    private fun handleScannerState(state: Int) {
        when (state) {
            BluetoothAdapter.STATE_TURNING_OFF, BluetoothAdapter.STATE_OFF -> {
                if (bleScanner != null && bleScanner!!.isScanning) {
                    bleScanner?.stopScan()
                }
            }
        }
    }


    companion object {

        fun from(context: Context): CoroutinesBLE {
            return CoroutinesBLE(context)
        }
    }
}
