package com.oncelabs.hpdemo.device.gatt

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import java.util.UUID

class BGM220PGatt {
    var throughputService: BluetoothGattService? = null
    var indicationsCharacteristic: BluetoothGattCharacteristic? = null
    var notificationsCharacteristic: BluetoothGattCharacteristic? = null
    var transmissionOnCharacteristic: BluetoothGattCharacteristic? = null
    var throughputResultCharacteristic: BluetoothGattCharacteristic? = null

    var throughputInformationService: BluetoothGattService? = null
    var connectionPhyCharacteristic: BluetoothGattCharacteristic? = null
    var connectionIntervalCharacteristic: BluetoothGattCharacteristic? = null
    var responderLatencyCharacteristic: BluetoothGattCharacteristic? = null
    var supervisionTimeoutCharacteristic: BluetoothGattCharacteristic? = null
    var pduSizeCharacteristic: BluetoothGattCharacteristic? = null
    var mtuSizeCharacteristic: BluetoothGattCharacteristic? = null

    fun foundService(service: BluetoothGattService) : Boolean {
        when (service.uuid) {
            ServiceUUIDs.THROUGHPUT_TEST.uuid -> {
                throughputService = service
                return true
            }
            ServiceUUIDs.THROUGHPUT_INFORMATION.uuid -> {
                throughputInformationService = service
                return true
            }
        }
        return false
    }

    fun foundCharacteristic(characteristic: BluetoothGattCharacteristic) : Boolean {
        when (characteristic.uuid) {
            CharacteristicUUIDs.INDICATIONS.uuid -> {
                indicationsCharacteristic = characteristic
                return true
            }
            CharacteristicUUIDs.NOTIFICATIONS.uuid -> {
                notificationsCharacteristic = characteristic
                return true
            }
            CharacteristicUUIDs.TRANSMISSION.uuid -> {
                transmissionOnCharacteristic = characteristic
                return true
            }
            CharacteristicUUIDs.THROUGHPUT_RESULT.uuid -> {
                throughputResultCharacteristic = characteristic
                return true
            }
            CharacteristicUUIDs.CONNECTION_PHY.uuid -> {
                connectionPhyCharacteristic = characteristic
                return true
            }
            CharacteristicUUIDs.CONNECTION_INTERVAL.uuid -> {
                connectionIntervalCharacteristic = characteristic
                return true
            }
            CharacteristicUUIDs.RESPONDER_LATENCY.uuid -> {
                responderLatencyCharacteristic = characteristic
                return true
            }
            CharacteristicUUIDs.SUPERVISION_TIMEOUT.uuid -> {
                supervisionTimeoutCharacteristic = characteristic
                return true
            }
            CharacteristicUUIDs.PDU_SIZE.uuid -> {
                pduSizeCharacteristic = characteristic
                return true
            }
            CharacteristicUUIDs.MTU_SIZE.uuid -> {
                mtuSizeCharacteristic = characteristic
                return true
            }
        }
        return false
    }
}