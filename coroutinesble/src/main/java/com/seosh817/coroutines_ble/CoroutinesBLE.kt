package com.seosh817.coroutines_ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import com.seosh817.coroutines_ble.logger.Logger
import com.seosh817.coroutines_ble.scanner.CrBLEScanner
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

@SuppressLint("MissingPermission")
class CoroutinesBLE(private val context: Context) {

    private var bluetoothManager: BluetoothManager? = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        bluetoothManager?.adapter
    }

    private var bleScanner: CrBLEScanner? = null

    private val adapterStateChannel = Channel<Int>(Channel.CONFLATED)

    private val adapterStateBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: android.content.Intent?) {
            val action = intent?.action ?: return
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                handleStateChange(state)
                adapterStateChannel.trySend(state)
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

    val isBluetoothEnabled: Boolean
        get() {
            if (bluetoothAdapter?.isEnabled == false) {
                return true
            }
            Logger.e("Bluetooth disabled")
            return false
        }

    var isScanning: Boolean = false
        private set

    init {
        bleScanner = CrBLEScanner.from(bluetoothAdapter)
        context.registerReceiver(adapterStateBroadcastReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
    }

    fun adapterStateFlow() = adapterStateChannel.receiveAsFlow()

    fun startScan(scanFilters: List<ScanFilter> = emptyList(), scanSettings: ScanSettings, scanCallback: ScanCallback) {
        try {
            if (!isBleSupported || !isBluetoothEnabled) {
                return
            }
            val scanner = checkNotNull(bluetoothAdapter?.bluetoothLeScanner) {
                "getBluetoothLeScanner() is null. Is the Adapter on?"
            }

            scanner.startScan(scanFilters, scanSettings, scanCallback)
        } catch (e: Exception) {
            Logger.e("startScan error $e")
        }
    }

    fun getSystemDevices(): Set<BluetoothDevice> = bluetoothAdapter?.bondedDevices ?: emptySet()

//    suspend fun scan(): Flow<BluetoothDevice> = CrBLEScanner.scanDevices(bluetoothAdapter)

    private fun handleStateChange(state: Int) {

        if (state == BluetoothAdapter.STATE_OFF || state == BluetoothAdapter.STATE_TURNING_OFF) {
            if (bleScanner != null && bleScanner!!.isScanning) {
                bleScanner?.stopScan()
            }
        }

        when (state) {
            BluetoothAdapter.STATE_TURNING_OFF, BluetoothAdapter.STATE_OFF -> {
                isScanning = false

            }

            BluetoothAdapter.STATE_TURNING_ON, BluetoothAdapter.STATE_ON -> {
                // Bluetooth is on
            }
        }
    }
}
