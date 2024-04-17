package com.oncelabs.hpdemo.device

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanResult
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Build
import android.util.Log
import com.oncelabs.hpdemo.device.gatt.BGM220PGatt
import java.util.UUID

val CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

@SuppressLint("MissingPermission")
class BGM220P(
    scanResult: ScanResult,
    context: Context
) {
    private val bgmGatt: BGM220PGatt = BGM220PGatt()
    private var bluetoothGatt: BluetoothGatt? = null
    private var servicesFound = false

    @OptIn(ExperimentalStdlibApi::class)
    private val gattCallback = object : BluetoothGattCallback() {

        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    bluetoothGatt = gatt
                    bluetoothGatt?.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    gatt.close()
                }
            } else {
                gatt.close()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                bluetoothGatt = gatt
                onSetupComplete()
            } else {
                Log.w(TAG, "onServicesDiscovered received: $status")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            with(characteristic) {
                Log.i("BluetoothGattCallback", "Characteristic ${characteristic.uuid} changed | value: ${value.toHexString()}")
            }
        }
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            val newValueHex = value.toHexString()
            Log.i("BluetoothGattCallback", "Characteristic ${characteristic.uuid} changed | value: $newValueHex")
        }
    }

    init {
        scanResult.device.connectGatt(context, false, gattCallback)
    }

    private fun onSetupComplete() {
        bluetoothGatt?.services?.let { services ->
            services.forEach { service ->
                if (bgmGatt.foundService(service)) {
                    service.characteristics.forEach {
                        if (bgmGatt.foundCharacteristic(it)) {
                            enableNotifications(it)
                        }
                    }
                }
            }
        }
    }

    private fun enableNotifications(characteristic: BluetoothGattCharacteristic) {
        bluetoothGatt?.let { gatt ->
            gatt.setCharacteristicNotification(characteristic, true)
            val payload = when {
                characteristic.isIndicatable() -> BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                characteristic.isNotifiable() -> BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                else -> {
                    return
                }
            }

            val descriptor = characteristic.getDescriptor(CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                gatt.writeDescriptor(
                    descriptor,
                    payload
                )
            } else {
                descriptor.value = payload
                gatt.writeDescriptor(descriptor)
            }
        }
    }

    private fun BluetoothGattCharacteristic.isIndicatable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_INDICATE)

    private fun BluetoothGattCharacteristic.isNotifiable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_NOTIFY)

    private fun BluetoothGattCharacteristic.containsProperty(property: Int): Boolean =
        properties and property != 0

}