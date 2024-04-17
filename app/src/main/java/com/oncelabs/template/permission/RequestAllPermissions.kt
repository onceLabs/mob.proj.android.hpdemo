package com.oncelabs.template.permission

import android.os.Build
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.oncelabs.template.modal.PermissionModal
import kotlinx.coroutines.launch

@ExperimentalPermissionsApi
@Composable
fun RequestAllPermissions(
    navigateToSettingsScreen: () -> Unit,
    onAllGranted: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val detailedPermissions = mutableMapOf<String, PermissionType>()
    for(permission in PermissionType.values()) {
        if(permission.minimumVersion == null || Build.VERSION.SDK_INT >= permission.minimumVersion) {
            detailedPermissions[permission.id] = permission
        }
    }

    val permissionStates = rememberMultiplePermissionsState(
        permissions = detailedPermissions.map { it.key }
    )

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(key1 = lifecycleOwner, effect = {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    permissionStates.launchMultiplePermissionRequest()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    })

    permissionStates.permissions.forEach { permissionState ->
        val detailedPermission = detailedPermissions[permissionState.permission]

        when {
            // If the camera permission is granted, then show screen with the feature enabled
            permissionState.hasPermission -> {
                if(permissionStates.allPermissionsGranted) {
                    onAllGranted()
                }
            }
            // If the user denied the permission but a rationale should be shown, or the user sees
            // the permission for the first time, explain why the feature is needed by the app and allow
            // the user to be presented with the permission again or to not see the rationale any more.
            //!permissionState.permissionRequested -> {
            permissionState.shouldShowRationale -> {
                if(detailedPermission?.implicit == false) {
                    var show by remember { mutableStateOf(true) }
                    PermissionModal(
                        shouldShow = show,
                        title = detailedPermission.title ?: "Unknown Permission",
                        body = detailedPermission.description ?: "Unknown Permission",
                        confirmText = "Request permission",
                        denyText = "Don't ask again",
                        onConfirm = { permissionState.launchPermissionRequest(); show = false },
                        onDeny = { show = false }
                    )
                } else {
                    scope.launch {
                        permissionState.launchPermissionRequest()
                    }
                }
            }

            // If the criteria above hasn't been met, the user denied the permission. Let's present
            // the user with a FAQ in case they want to know more and send them to the Settings screen
            // to enable it the future there if they want to.
            //permissionState.shouldShowRationale -> {
            !permissionState.hasPermission && !permissionState.shouldShowRationale -> {
                if(detailedPermission?.implicit == false) {
                    var show by remember { mutableStateOf(true) }

                    PermissionModal(
                        shouldShow = show,
                        title = "${detailedPermission.title} Required",
                        body = "The ${detailedPermission.title} permission is" +
                                " required to enable all features of this application. Please enable this permission in Settings.",
                        confirmText = "Open Settings",
                        denyText = "Close",
                        onConfirm = { navigateToSettingsScreen() },
                        onDeny = { show = false }
                    )
                } else {
                    scope.launch {
                        permissionState.launchPermissionRequest()
                    }
                }
            }
        }
    }
}