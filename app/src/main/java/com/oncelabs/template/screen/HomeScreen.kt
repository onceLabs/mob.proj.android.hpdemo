package com.oncelabs.template.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.oncelabs.template.viewModel.HomeViewModel

/**
 * Home Screen of the application
 * - [State hoisting](https://developer.android.com/jetpack/compose/state#state-hoisting)
 * - Preview Configured
 */
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    // Hoist your states & only pass what you need!
    // NEVER pass the viewModel
    val importantMessage by homeViewModel.importantMessage.observeAsState(initial = "")

    HomeContent(
        importantMessage = importantMessage
    )
}

@Composable
private fun HomeContent(
    importantMessage: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = importantMessage,
            color = MaterialTheme.colors.onBackground
        )
    }
}

/**
 * All relevant screen sizes to enforce responsive design
 */
@Preview
@Preview(name = "NEXUS_7", device = Devices.NEXUS_7)
@Preview(name = "NEXUS_5", device = Devices.NEXUS_5)
@Preview(name = "PIXEL_C", device = Devices.PIXEL_C)
@Preview(name = "PIXEL_2", device = Devices.PIXEL_2)
@Preview(name = "PIXEL_4", device = Devices.PIXEL_4)
@Preview(name = "PIXEL_4_XL", device = Devices.PIXEL_4_XL)
@Composable
fun HomePreview() {
    HomeContent(
        importantMessage = "Previewed message"
    )
}