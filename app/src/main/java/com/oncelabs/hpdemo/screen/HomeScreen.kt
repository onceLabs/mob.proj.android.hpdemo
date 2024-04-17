package com.oncelabs.hpdemo.screen

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
import androidx.hilt.navigation.compose.hiltViewModel
import com.oncelabs.hpdemo.enums.TXMethod
import com.oncelabs.hpdemo.ui.theme.buttonColor
import com.oncelabs.hpdemo.ui.theme.gray
import com.oncelabs.hpdemo.viewModel.HomeViewModel

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
    val txMethod: MutableState<TXMethod> = remember {
        mutableStateOf(TXMethod.NOTIFICATIONS)
    }

    HomeContent(
        isScanning.value,
        { homeViewModel.stopScan() },
        { homeViewModel.startScan() },
        { isScanning.value = it },
        txMethod.value,
        { txMethod.value = it }
    )
}

@Composable
private fun HomeContent(
    isScanning: Boolean,
    stopScan: () -> Unit,
    startScan: () -> Unit,
    setIsScanning: (Boolean) -> Unit,
    txMethod: TXMethod,
    changeTXMethod: (TXMethod) -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(start = 50.dp, end = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = gray)
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
            Text(if (isScanning) "Stop Scan" else "Start Scan", Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(5.dp))
        Button(
            onClick = {
            },
            shape = RoundedCornerShape(5.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor,
                contentColor = Color.White
            )
        ) {
            Text("Begin Test", Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(25.dp))
        TabRow(
            selectedTabIndex = if (TXMethod.NOTIFICATIONS == txMethod) 0 else 1,
            containerColor = gray,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .height(40.dp),
            indicator = { tabPositions: List<TabPosition> ->
                Box {}
            }
        ) {
            TXMethod.entries.forEach {
                val selected = txMethod == it
                Tab(
                    modifier = if (selected) Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            buttonColor
                        )
                    else Modifier
                        .padding(4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            gray
                        ),
                    selected = selected,
                    onClick = { changeTXMethod(it) },
                    text = { Text(text = it.title, color = if (selected) Color.White else Color.Black) }
                )
            }
        }
        Spacer(Modifier.height(5.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text("Transmit Method: ${txMethod.title}")
        }
        Spacer(Modifier.height(25.dp))
        Column(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth()) {
                Text("Connection Interval:")
                Spacer(Modifier.weight(1f))
                Text("30ms")
            }
            Row(Modifier.fillMaxWidth()) {
                Text("PHY:")
                Spacer(Modifier.weight(1f))
                Text("2M")
            }
            Row(Modifier.fillMaxWidth()) {
                Text("MTU:")
                Spacer(Modifier.weight(1f))
                Text("247")
            }
        }
        Spacer(Modifier.height(25.dp))
        Column(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth()) {
                Text("Bytes Sent:")
                Spacer(Modifier.weight(1f))
                Text("12,394")
            }
            Row(Modifier.fillMaxWidth()) {
                Text("Elapsed Time:")
                Spacer(Modifier.weight(1f))
                Text("12.34 Seconds")
            }
            Row(Modifier.fillMaxWidth()) {
                Text("Avg Throughput:")
                Spacer(Modifier.weight(1f))
                Text("328kbps")
            }
        }
    }
}

