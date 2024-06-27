package com.seosh817.coroutines_ble.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seosh817.coroutines_ble.CoroutinesBLE
import kotlinx.coroutines.launch

class BLEViewModel(private val coroutinesBLE: CoroutinesBLE) : ViewModel() {
    val isScanning = mutableStateOf(false)

    fun startScan() {
        viewModelScope.launch {
            coroutinesBLE.startScan()
        }
    }

    fun stopScan() {
        viewModelScope.launch {
            coroutinesBLE.stopScan()
        }
    }
}