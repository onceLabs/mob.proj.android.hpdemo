package com.oncelabs.hpdemo.manager

import android.content.Context
import java.lang.ref.WeakReference

interface DeviceManager {
    fun init(getContext: WeakReference<Context>)
    fun startScan()
    fun stopScan()
}