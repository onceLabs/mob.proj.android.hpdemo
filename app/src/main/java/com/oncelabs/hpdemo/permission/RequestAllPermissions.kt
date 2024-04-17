package com.oncelabs.hpdemo.permission

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.oncelabs.hpdemo.ui.theme.doneButtonColor
import com.oncelabs.hpdemo.ui.theme.errorModalTitleFont
import com.oncelabs.hpdemo.ui.theme.permissionModalBackgroundColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@ExperimentalPermissionsApi
@Composable
fun RequestAllPermissions(
    startBluetooth: () -> Unit,
    onAllGranted: @Composable () -> Unit,
) {

    val permissions = rememberMultiplePermissionsState(
        permissions = PermissionType.entries.map { it.id }
    )

    if (permissions.allPermissionsGranted) {
        onAllGranted()
    }
    LaunchedEffect(permissions.allPermissionsGranted) {
        if (permissions.allPermissionsGranted) {
            startBluetooth()
        }
    }
    if (!permissions.allPermissionsGranted) {
        for (p in PermissionType.values().indices) {
            val permissionState = rememberPermissionState(PermissionType.values()[p].id)
            if (!permissionState.hasPermission) {
                PermissionModalHolder(
                    permissionState,
                    permission = PermissionType.values()[p],
                )
                break;
            }
        }

    }

}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionModalHolder(
    permissionState: PermissionState,
    permission: PermissionType
) {
    val context = LocalContext.current
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    )
    LaunchedEffect(permissionState.hasPermission) {
        delay(500)
        permissionState.launchPermissionRequest()
    }
    PermissionModal(
        shouldShow = !permissionState.hasPermission && permissionState.permissionRequested,
        title = permission.title,
        body = permission.description,
        confirmText = if (permissionState.permissionRequested) "Open Settings" else "Open Dialog",
        onOpenDialog = {
            if (permissionState.permissionRequested) {
                context.startActivity(intent)
            } else {
                permissionState.launchPermissionRequest()
            }
        })

}

@Composable
fun PermissionModal(
    shouldShow: Boolean,
    title: String,
    body: String,
    confirmText: String,
    onOpenDialog: () -> Unit,
) {
    if (shouldShow) {
        Dialog(
            onDismissRequest = {},
            DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .background(
                        permissionModalBackgroundColor,
                        shape = RoundedCornerShape(2.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Row(Modifier.fillMaxWidth()) {
                        Text(text = title, style = errorModalTitleFont)
                    }
                    Row(Modifier.fillMaxWidth()) {
                        Text(text = body)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Spacer(Modifier.weight(1f))
                        Text(confirmText, color = doneButtonColor, modifier = Modifier.clickable {
                            onOpenDialog()
                        })

                    }
                }
            }
        }
    }
}