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
import com.oncelabs.hpdemo.device.gatt.CharacteristicUUIDs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.Queue
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue

val CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID =
    UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

enum class GattOperationType {
    READ,
    WRITE,
    NOTIFY,
    INDICATE,
    ENABLE_NOTIFICATION,
    ENABLE_INDICATION,
    DISABLE_NOTIFICATION,
    DISABLE_INDICATION,
    SERVICE_DISCOVERY,
    MTU_UPDATE
}

class GattOperation(
    val type: GattOperationType,
    val operation: () -> Boolean
)

@SuppressLint("MissingPermission")
class BGM220P(
    scanResult: ScanResult,
    context: Context
) {
    private var gattOperationScope = CoroutineScope(Dispatchers.IO)
    private val bgmGatt: BGM220PGatt = BGM220PGatt()
    private var bluetoothGatt: BluetoothGatt? = null
    private var servicesFound = false

    private var gattOperationQueue: Queue<GattOperation> = ConcurrentLinkedQueue()
    private var pendingGattOperation: GattOperation? = null

    @OptIn(ExperimentalStdlibApi::class)
    private val gattCallback = object : BluetoothGattCallback() {

        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    bluetoothGatt = gatt
                    enqueueOperation(GattOperation(GattOperationType.SERVICE_DISCOVERY) {
                        return@GattOperation gatt.discoverServices()
                    })
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
            // Check if pending operation is not null and operation type is SERVICE_DISCOVERY
            if (pendingGattOperation != null && pendingGattOperation?.type == GattOperationType.SERVICE_DISCOVERY) {
                Log.i("BluetoothGattCallback", "Service discovery status: $status")
                signalEndOfOperation()
            }
        }


        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            val newValueHex = value.toHexString()
            Log.i(
                "BluetoothGattCallback",
                "Characteristic ${characteristic.uuid} changed | value: $newValueHex"
            )
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            // Check if pending operation is not null and operation type is WRITE
            if (pendingGattOperation != null && pendingGattOperation?.type == GattOperationType.WRITE) {
                Log.i(
                    "BluetoothGattCallback",
                    "Characteristic ${characteristic?.uuid} write status: $status"
                )
                signalEndOfOperation()
            }
        }


        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, value, status)
            // Check if pending operation is not null and operation type is READ
            if (pendingGattOperation != null && pendingGattOperation?.type == GattOperationType.READ) {
                Log.d(
                    "BluetoothGattCallback",
                    "Characteristic ${characteristic.uuid} read status: $status")
                signalEndOfOperation()
            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)
            Log.d("BluetoothGattCallback", "Descriptor ${descriptor?.uuid} write status: $status")
            // Check if pending operation is not null and operation type is ENABLE_NOTIFICATION or ENABLE_INDICATION
            if (pendingGattOperation != null && (pendingGattOperation?.type == GattOperationType.ENABLE_NOTIFICATION || pendingGattOperation?.type == GattOperationType.ENABLE_INDICATION)) {
                Log.d(
                    "BluetoothGattCallback",
                    "Descriptor ${descriptor?.uuid} write status: $status")
                signalEndOfOperation()
            }
        }

        override fun onDescriptorRead(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int,
            value: ByteArray
        ) {
            super.onDescriptorRead(gatt, descriptor, status, value)
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
                            enqueueOperation(GattOperation(GattOperationType.ENABLE_NOTIFICATION) {
                                enableNotifications(it)
                                return@GattOperation true
                            })
                        }
                    }
                }
            }
        }
        readThroughputInformation()
    }

    private fun readThroughputInformation() {
        enqueueOperation(GattOperation(GattOperationType.READ) {
            return@GattOperation bluetoothGatt?.readCharacteristic(bgmGatt.connectionIntervalCharacteristic) ?: false
        })
        enqueueOperation(GattOperation(GattOperationType.READ) {
            return@GattOperation bluetoothGatt?.readCharacteristic(bgmGatt.responderLatencyCharacteristic) ?: false
        })
        enqueueOperation(GattOperation(GattOperationType.READ) {
            return@GattOperation bluetoothGatt?.readCharacteristic(bgmGatt.supervisionTimeoutCharacteristic) ?: false
        })
        enqueueOperation(GattOperation(GattOperationType.READ) {
            return@GattOperation bluetoothGatt?.readCharacteristic(bgmGatt.connectionPhyCharacteristic) ?: false
        })
        enqueueOperation(GattOperation(GattOperationType.READ) {
            return@GattOperation bluetoothGatt?.readCharacteristic(bgmGatt.pduSizeCharacteristic) ?: false
        })
        enqueueOperation(GattOperation(GattOperationType.READ) {
            return@GattOperation bluetoothGatt?.readCharacteristic(bgmGatt.mtuSizeCharacteristic) ?: false
        })
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

            val descriptor =
                characteristic.getDescriptor(CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID)
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

    enum class PHY(val string: String) {
        PHY_1M("1M"),
        PHY_2M("2M"),
        PHY_CODED_125K("Coded 125k"),
        PHY_CODED_500K("Coded 500k"),
        UNKNOWN("Unknown")
    }

    data class ConnectionParameters(
        val mtu: Int,
        val pdu: Int,
        val interval: Double,
        val latency: Int,
        val supervisionTimeout: Int,
    )

    sealed class ConnectionParameter {
        class phy(val phy: PHY) : ConnectionParameter()
        class connectionInterval(val value: Double) : ConnectionParameter()
        class latency(val value: Double) : ConnectionParameter()
        class supervisionTimeout(value: Double) : ConnectionParameter()
        class pdu(value: Int) : ConnectionParameter()
        class mtu(value: Int) : ConnectionParameter()
        class unknown() : ConnectionParameter()
    }


    interface SILThroughputConnectionParametersDecoderType {
        fun decode(data: ByteArray, characterisitc: UUID): ConnectionParameter
    }

    class SILThroughputConnectionParametersDecoder {
        private val CONNECTION_INTERVAL_STEP = 1.25
        private val SLAVE_LATENCY_STEP = 1.25
        private val SUPERVISION_TIMEOUT_STEP = 10
        fun decode(data: ByteArray, characterisitc: UUID): ConnectionParameter {
            return when (characterisitc) {
                CharacteristicUUIDs.CONNECTION_PHY.uuid -> ConnectionParameter.phy(
                    decodePHY(
                        data[0]
                    )
                )
                CharacteristicUUIDs.CONNECTION_INTERVAL.uuid -> ConnectionParameter.connectionInterval(
                    decodeConnectionInterval(data)
                )
                CharacteristicUUIDs.RESPONDER_LATENCY.uuid -> ConnectionParameter.latency(
                    decodeLatency(data)
                )
                CharacteristicUUIDs.SUPERVISION_TIMEOUT.uuid -> ConnectionParameter.supervisionTimeout(
                    decodeSupervision(data)
                )
                CharacteristicUUIDs.PDU_SIZE.uuid -> { ConnectionParameter.pdu(
                    decodePDU(data[0]))
                }
                CharacteristicUUIDs.MTU_SIZE.uuid -> { ConnectionParameter.mtu(
                    decodeMTU(data[0]))
                }
                else -> {
                    ConnectionParameter.unknown()
                }
            }
        }

        private fun decodePHY(value: Byte): PHY {
            return when (value.toInt()) {
                0x01 -> PHY.PHY_1M
                0x02 -> PHY.PHY_2M
                0x04 -> PHY.PHY_CODED_125K
                0x08 -> PHY.PHY_CODED_500K
                else -> PHY.UNKNOWN
            }
        }

        private fun decodeConnectionInterval(value: ByteArray): Double {
            return byteArrayToInt(value) * CONNECTION_INTERVAL_STEP
        }

        private fun decodeLatency(value: ByteArray): Double {
            return byteArrayToInt(value) * SLAVE_LATENCY_STEP
        }

        private fun decodeSupervision(value: ByteArray): Double {
            return byteArrayToInt(value).toDouble() * SUPERVISION_TIMEOUT_STEP
        }

        private fun decodePDU(value: Byte): Int {
            return value.toInt() and 0xFF
        }

        private fun decodeMTU(value: Byte): Int {
            return value.toInt() and 0xFF
        }

        private fun byteArrayToInt(bytes: ByteArray): Int {
            var result = 0
            for (i in bytes.indices) {
                result = result or ((bytes[i].toInt() and 0xFF) shl 8 * i)
            }
            return result
        }
    }

    @Synchronized
    private fun enqueueOperation(operation: GattOperation) {
        gattOperationQueue.add(operation)
        if (pendingGattOperation == null) {
            doNextOperation()
        }
    }

    @Synchronized
    private fun signalEndOfOperation() {
        print("OBPeripheral: request completed $pendingGattOperation")
        pendingGattOperation = null
        if (gattOperationQueue.isNotEmpty()) {
            doNextOperation()
        }
    }

    @Synchronized
    private fun doNextOperation() {
        Log.d(TAG, "BGM220GattQueue: doNextOperation")
        if (pendingGattOperation != null) {
            print("OBPeripheral: pending request, abort doNextOperation")
            return
        }

        val gattRequest = gattOperationQueue.poll() ?: run {
            print("OBPeripheral: GATT request queue empty ")
            return
        }
        pendingGattOperation = gattRequest

        when (gattRequest.type) {
            GattOperationType.WRITE -> {
                print("Write")
            }
            GattOperationType.READ -> {
                print("Read")
            }
            GattOperationType.ENABLE_INDICATION -> {
                print("Enable Indication")
            }
            GattOperationType.DISABLE_INDICATION -> {
                print("Disable Indication")
            }
            GattOperationType.ENABLE_NOTIFICATION -> {
                print("Enable Notification")
            }
            GattOperationType.DISABLE_NOTIFICATION -> {
                print("Disable Notification")
            }
            GattOperationType.MTU_UPDATE -> {
                print("MTU Update request")
            }
            else -> {}
        }
        if (gattRequest.operation.invoke()){
            // TODO: Add call to log for analytics
        } else {
            // TODO: Add call to log for analytics
            // Failed, move on to next so we don't clog the queue
            // need to signal an error
            signalEndOfOperation()
        }
    }

}