package com.seosh817.coroutines_ble.viewmodel

import android.bluetooth.le.ScanResult
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seosh817.coroutines_ble.BleScanResult
import com.seosh817.coroutines_ble.CoroutinesBle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BLEViewModel @Inject constructor(private val coroutinesBLE: CoroutinesBle) : ViewModel() {

    private val _isScanning: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean>
        get() = _isScanning.asStateFlow()

    private val _scanResults: MutableStateFlow<List<BleScanResult>> = MutableStateFlow(listOf())
    val scanResults: StateFlow<List<BleScanResult>>
        get() = _scanResults.asStateFlow()

    private var scanJob: Job? = null


    fun startScan() {
        if (_isScanning.value) return
        scanJob = viewModelScope.launch {
            coroutinesBLE.startScan()
                .onStart {
                    Log.d("!!!", "onStart")
                    _isScanning.value = true
                }
                .onCompletion {
                    Log.d("!!!", "onCompletion")
                    _isScanning.value = false
                }
                .collectLatest {
                    Log.d("!!!", "devices: ${it.map { it.device.address }}")
                    _scanResults.value = it
                }
        }
    }

    fun stopScan() {
        scanJob?.cancel()
    }
}