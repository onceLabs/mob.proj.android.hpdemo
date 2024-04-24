package com.oncelabs.hpdemo.manager

import android.content.Context
import com.oncelabs.hpdemo.device.BGM220P
import kotlinx.coroutines.flow.StateFlow
import java.lang.ref.WeakReference

interface DeviceManager {

    val selectedDevice: StateFlow<BGM220P?>
    fun init(getContext: WeakReference<Context>)
    fun startScan()
    fun stopScan()
}