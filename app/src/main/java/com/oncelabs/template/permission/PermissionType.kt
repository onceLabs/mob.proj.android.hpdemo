package com.oncelabs.template.permission

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import java.lang.Exception

@SuppressLint("InlinedApi")
enum class PermissionType(
    val id: String,
    val title: String,
    val description: String,
    val minimumVersion: Int?,
    val implicit: Boolean
) {
    ACCESS_FINE_LOCATION(
        Manifest.permission.ACCESS_FINE_LOCATION,
        "Fine Location",
        "This permission is required to discover devices nearby.",
        null,
        false),
    BLUETOOTH_SCAN(
        Manifest.permission.BLUETOOTH_SCAN,
        "Bluetooth Scan",
        "This permission is required to scan for nearby devices.",
        31,
        false),
    BLUETOOTH_CONNECT(
        Manifest.permission.BLUETOOTH_CONNECT,
        "Bluetooth Connect",
        "This permission is required to connect to a nearby device.",
        31,
        false);

    companion object {
        private val TAG: String = PermissionType::class.java.simpleName

        fun navigateToSettings(context: Context) {
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", context.packageName, null)
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Error occurred when navigating to settings: ${e.localizedMessage}")
            }
        }
    }
}