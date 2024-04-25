package com.oncelabs.hpdemo.screen

import android.widget.Spinner
import android.widget.SpinnerAdapter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role.Companion.Button
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.widget.ContentLoadingProgressBar
import androidx.hilt.navigation.compose.hiltViewModel
import com.oncelabs.hpdemo.device.BGM220P
import com.oncelabs.hpdemo.enums.TXMethod
import com.oncelabs.hpdemo.ui.theme.buttonColor
import com.oncelabs.hpdemo.ui.theme.gray
import com.oncelabs.hpdemo.viewModel.HomeViewModel


data class ThroughputInformation(
    val elapsedTime: Int?,
    val bytesReceived: Int?,
    val throughput: Int?,
    val phy: BGM220P.PHY?,
    val connectionInterval: Double?,
    val latency: Int?,
    val supervisionTimeout: Int?,
    val pduSize: Int?,
    val mtuSize: Int?,
    val testActive: Boolean?
)
/**
 * Home Screen of the application
 * - [State hoisting](https://developer.android.com/jetpack/compose/state#state-hoisting)
 * - Preview Configured
 */
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val isScanning: MutableState<Boolean> = remember {
        mutableStateOf(false)
    }
    val elapsedTime = homeViewModel.elapsedTime.observeAsState()
    val bytesReceived = homeViewModel.bytesReceived.observeAsState()
    val throughput = homeViewModel.throughput.observeAsState()
    val phy = homeViewModel.phy.observeAsState()
    val connectionInterval = homeViewModel.connectionInterval.observeAsState()
    val latency = homeViewModel.latency.observeAsState()
    val supervisionTimeout = homeViewModel.superVisionTimeout.observeAsState()
    val pduSize = homeViewModel.pduSize.observeAsState()
    val mtuSize = homeViewModel.mtuSize.observeAsState()
    val testActive = homeViewModel.testInProgress.observeAsState()

    HomeContent(
        isScanning.value,
        { homeViewModel.stopScan() },
        { homeViewModel.startScan() },
        { isScanning.value = it },
        ThroughputInformation(
            elapsedTime.value?.div(1000),
            bytesReceived.value,
            throughput.value,
            phy.value,
            connectionInterval.value,
            latency.value,
            supervisionTimeout.value,
            pduSize.value,
            mtuSize.value,
            testActive.value
        )
    )
}

@Composable
private fun HomeContent(
    isScanning: Boolean,
    stopScan: () -> Unit,
    startScan: () -> Unit,
    setIsScanning: (Boolean) -> Unit,
    throughputInformation: ThroughputInformation
) {
    Column(
        Modifier
            .background(gray)
            .fillMaxSize()
            .padding(start = 50.dp, end = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        throughputInformation.testActive?.let {
            if (it){

            }
        }
        Spacer(Modifier.height(25.dp))
        Button(
            onClick = {
                if (isScanning) {
                    stopScan()
                } else {
                    startScan()
                }
                setIsScanning(!isScanning)
            },
            shape = RoundedCornerShape(5.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor,
                contentColor = Color.White
            )
        ) {
            Text(if (isScanning) "Disconnect" else "Connect", Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        }

        Spacer(Modifier.height(25.dp))
        Column(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth()) {
                Text("Connection Interval:")
                Spacer(Modifier.weight(1f))
                Text("${throughputInformation.connectionInterval}ms")
            }
            Row(Modifier.fillMaxWidth()) {
                Text("PHY:")
                Spacer(Modifier.weight(1f))
                Text("${throughputInformation.phy}")
            }
            Row(Modifier.fillMaxWidth()) {
                Text("MTU:")
                Spacer(Modifier.weight(1f))
                Text("${throughputInformation.mtuSize}")
            }
        }
        Spacer(Modifier.height(25.dp))
        Column(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth()) {
                Text("Bytes Received:")
                Spacer(Modifier.weight(1f))
                Text("${throughputInformation.bytesReceived}")
            }
            Row(Modifier.fillMaxWidth()) {
                Text("Elapsed Time:")
                Spacer(Modifier.weight(1f))
                Text("${throughputInformation.elapsedTime} Seconds")
            }
            Row(Modifier.fillMaxWidth()) {
                Text("Avg Throughput:")
                Spacer(Modifier.weight(1f))
                Text("${throughputInformation.throughput}kbps")
            }
        }
    }
}

// Add preview for the HomeScreen
@Preview(device = Devices.PIXEL_4_XL)
@Composable
fun HomeScreenPreview() {
    HomeContent(
        isScanning = false,
        stopScan = {},
        startScan = {},
        setIsScanning = {},
        ThroughputInformation(
            0,
            0,
            0,
            BGM220P.PHY.PHY_1M,
            0.0,
            0,
            0,
            0,
            0,
            true
        )
    )
}
