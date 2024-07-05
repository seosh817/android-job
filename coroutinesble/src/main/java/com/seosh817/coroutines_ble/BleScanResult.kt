package com.seosh817.coroutines_ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanRecord
import android.bluetooth.le.ScanResult
import android.os.Build
import androidx.annotation.RequiresApi

data class BleScanResult(
    val device: BluetoothDevice,
    val scanRecord: ScanRecord?,
    val rssi: Int,
    val timestampNanos: Long,

    @RequiresApi(Build.VERSION_CODES.O)
    val txPower: Int? = null,
    @RequiresApi(Build.VERSION_CODES.O)
    val advertisingSid: Int? = null,
    @RequiresApi(Build.VERSION_CODES.O)
    val primaryPhy: Phy? = null,
    @RequiresApi(Build.VERSION_CODES.O)
    val secondaryPhy: Phy? = null,
    @RequiresApi(Build.VERSION_CODES.O)
    val dataStatus: Int? = null,
    @RequiresApi(Build.VERSION_CODES.O)
    val periodicAdvertisingInterval: Int? = null,
    @RequiresApi(Build.VERSION_CODES.O)
    val isConnectable: Boolean? = null,
    @RequiresApi(Build.VERSION_CODES.O)
    val isLegacy: Boolean? = null,
)

fun ScanResult.toBleScanResult(): BleScanResult {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        toRecentScanResult()
    } else {
        toLegacyScanResult()
    }
}


fun ScanResult.toLegacyScanResult(): BleScanResult {
    return BleScanResult(
        device = device,
        scanRecord = scanRecord,
        rssi = rssi,
        timestampNanos = timestampNanos
    )
}

@RequiresApi(Build.VERSION_CODES.O)
fun ScanResult.toRecentScanResult(): BleScanResult {
    return BleScanResult(
        device = device,
        scanRecord = scanRecord,
        rssi = rssi,
        timestampNanos = timestampNanos,
        txPower = txPower,
        advertisingSid = advertisingSid,
        primaryPhy = Phy.fromValue(primaryPhy) ?: if (isLegacy) Phy.PHY_LE_1M else Phy.PHY_LE_CODED,
        secondaryPhy = Phy.fromValue(secondaryPhy),
        dataStatus = dataStatus,
        periodicAdvertisingInterval = periodicAdvertisingInterval,
        isLegacy = isLegacy,
        isConnectable = isConnectable
    )
}