package com.seosh817.coroutines_ble

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.seosh817.coroutines_ble.ui.theme.CoroutinesBLETheme
import com.seosh817.coroutines_ble.viewmodel.BLEViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: BLEViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CoroutinesBLETheme {

                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Column {
                        BLEScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Composable
fun BLEScreen(viewModel:BLEViewModel = hiltViewModel()) {
    val isScanning by viewModel.isScanning.collectAsState()
    val devices by viewModel.scanResults.collectAsState()

    Column {
        Button(onClick = {
            if (isScanning) viewModel.stopScan() else viewModel.startScan()}) {
            Text(if (isScanning) "Stop Scan" else "Start Scan")
        }

        LazyColumn {
            items(devices) { device ->
                Text(text = device.device.address)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CoroutinesBLETheme {
        Greeting("Android")
    }
}
