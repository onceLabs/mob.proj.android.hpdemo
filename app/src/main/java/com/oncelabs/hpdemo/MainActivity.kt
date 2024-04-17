package com.oncelabs.hpdemo

import android.content.ComponentName
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.oncelabs.hpdemo.manager.DeviceManager
import com.oncelabs.hpdemo.permission.PermissionType
import com.oncelabs.hpdemo.permission.RequestAllPermissions
import com.oncelabs.hpdemo.screen.HomeScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * This is a Single Activity application,
 * try and keep this file as clean as possible
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var deviceManager: DeviceManager


    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RequestAllPermissions(startBluetooth = {

            }) {
                HomeScreen()
            }
        }
    }


}

