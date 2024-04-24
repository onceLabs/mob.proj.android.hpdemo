package com.oncelabs.hpdemo.manager.implementation

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.oncelabs.hpdemo.device.BGM220P
import com.oncelabs.hpdemo.enums.BleState
import com.oncelabs.hpdemo.enums.ScanState
import com.oncelabs.hpdemo.manager.DeviceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceManagerImpl @Inject constructor(
) : DeviceManager {

    private val _selectedDevice = MutableStateFlow<BGM220P?>(null)
    override val selectedDevice: StateFlow<BGM220P?> = _selectedDevice.asStateFlow()

    private val leDeviceMap: ConcurrentMap<String, BGM220P> = ConcurrentHashMap()

    private var bleStateFlow = MutableSharedFlow<BleState?>()

    private var context: WeakReference<Context>? = null

    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothLeScanner: BluetoothLeScanner? = null

    private val _scanState = MutableStateFlow(ScanState.UNKNOWN)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    private val scanFilters: MutableList<ScanFilter> by lazy {
        val _scanFilters = mutableListOf<ScanFilter>(
            ScanFilter.Builder()
                .build(),
            ScanFilter.Builder()
                .build()
        )
        _scanFilters
    }

    private val scanSettings: ScanSettings by lazy {
        ScanSettings.Builder()
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES) // Report all advertisements
            .setLegacy(false) // Report legacy in addition to extended
            .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE) // Report matches even when signal level may be low
            .setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT) // Report as many advertisments as possible
            .setPhy(ScanSettings.PHY_LE_ALL_SUPPORTED) // Use all available PHYs
            .setReportDelay(0)// Deliver results immediately
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY) // Use highest duty cycle
            .build() // Finished

    }

    private val beaconScope = CoroutineScope(Dispatchers.IO)

    override fun init(getContext: WeakReference<Context>) {
        context = getContext
        bluetoothManager =
            (getContext.get()?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager)
        bluetoothAdapter = bluetoothManager?.adapter
        bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
        setupBluetoothAdapterStateHandler()
    }

    @SuppressLint("MissingPermission")
    override fun startScan() {
        if (_scanState.value == ScanState.SCANNING) return
        bluetoothAdapter?.let { adapter ->
            if (!adapter.isEnabled) {
                return
            }
            bluetoothLeScanner
                ?.startScan(
                    scanFilters,
                    scanSettings,
                    leScanCallback
                )

            _scanState.value = ScanState.SCANNING
            Log.d(TAG, "Starting scan")
        } ?: run {
            Log.d(TAG, "Cannot start scanning. Bluetooth adapter is null")
        }    }

    @SuppressLint("MissingPermission")
    override fun stopScan() {
        bluetoothLeScanner?.stopScan(leScanCallback)
        _scanState.value = ScanState.STOPPED
        Log.d(TAG, "Starting scan")    }

    private fun setupBluetoothAdapterStateHandler() {
        val bluetoothAdapterStateReceiver: BroadcastReceiver = object : BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent) {
                // Verify the action matches what we are looking for
                if (intent.action == BluetoothAdapter.ACTION_STATE_CHANGED) {

                    val previousState = intent.getIntExtra(
                        BluetoothAdapter.EXTRA_PREVIOUS_STATE,
                        BluetoothAdapter.ERROR
                    )

                    val currentState = intent.getIntExtra(
                        BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR
                    )

                    when (currentState) {
                        BluetoothAdapter.STATE_OFF -> {
                            Log.d(TAG, "BluetoothAdapter State: Off")
                            beaconScope.launch {
                                bleStateFlow.emit(BleState.UNAVAILABLE)
                            }
                        }
                        BluetoothAdapter.STATE_TURNING_OFF ->
                            Log.d(TAG, "BluetoothAdapter State: Turning off")
                        BluetoothAdapter.STATE_ON -> {
                            Log.d(TAG, "BluetoothAdapter State: On")
                            beaconScope.launch {
                                bleStateFlow.emit(BleState.AVAILABLE)
                            }
                        }
                        BluetoothAdapter.STATE_TURNING_ON ->
                            Log.d(TAG, "BluetoothAdapter State: Turning on")
                    }
                }
            }
        }

        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        context?.get()?.let {
            (it).registerReceiver(bluetoothAdapterStateReceiver, filter)
        }
    }

    private val leScanCallback: ScanCallback by lazy {
        object : ScanCallback() {
            @SuppressLint("MissingPermission")
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                super.onScanResult(callbackType, result)
                context?.get()?.let { context ->
                    result?.device?.address?.let { deviceAddress ->
                        // Check for existing entry
                        if (!leDeviceMap.containsKey(deviceAddress)) {
                            if (result.device?.name == "Throughput Test") {
                                val device = BGM220P(result, context)
                                leDeviceMap[deviceAddress] = device
                            } else {
                                //Log.d("TEST", "DEVICE FOUND ${result.device.address}")
                            }
                        } else {

                        }
                    }
                }
            }

            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                Log.d(TAG, "BLE Scan Failed with ErrorCode: $errorCode")
                _scanState.value = ScanState.FAILED
            }
        }
    }
}