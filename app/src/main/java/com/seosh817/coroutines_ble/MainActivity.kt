package com.seosh817.coroutines_ble

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.seosh817.coroutines_ble.ui.theme.CoroutinesBLETheme
import com.seosh817.coroutines_ble.viewmodel.BLEViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CoroutinesBLETheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Greeting("Android")
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
fun BLEScreen() {
    val viewModel: BLEViewModel = viewModel()
    val isScanning by viewModel.isScanning.collectAsState()

    Button(onClick = {
        if (isScanning) {
            viewModel.stopScan()
        } else {
            viewModel.startScan()
        }
    }) {
        Text(if (isScanning) "Stop Scan" else "Start Scan")
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CoroutinesBLETheme {
        Greeting("Android")
    }
}

