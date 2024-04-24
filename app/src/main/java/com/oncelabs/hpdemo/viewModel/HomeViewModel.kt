package com.oncelabs.hpdemo.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.oncelabs.hpdemo.device.BGM220P
import com.oncelabs.hpdemo.manager.DeviceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    private val deviceManager: DeviceManager
): AndroidViewModel(application) {

    private val _isScanning: MutableLiveData<Boolean> = MutableLiveData(false)
    val isScanning: LiveData<Boolean> = _isScanning

    private val _elapsedTime: MutableLiveData<Int> = MutableLiveData(0)
    val elapsedTime: LiveData<Int> = _elapsedTime

    private val _bytesReceived: MutableLiveData<Int> = MutableLiveData(0)
    val bytesReceived: LiveData<Int> = _bytesReceived

    private val _throughput: MutableLiveData<Int> = MutableLiveData(0)
    val throughput: LiveData<Int> = _throughput

    private val _phy: MutableLiveData<BGM220P.PHY> = MutableLiveData(BGM220P.PHY.UNKNOWN)
    val phy: LiveData<BGM220P.PHY> = _phy

    private val _connectionInterval: MutableLiveData<Int> = MutableLiveData(0)
    val connectionInterval: LiveData<Int> = _connectionInterval

    private val _latency: MutableLiveData<Int> = MutableLiveData(0)
    val latency: LiveData<Int> = _latency

    private val _superVisionTimeout: MutableLiveData<Int> = MutableLiveData(0)
    val superVisionTimeout: LiveData<Int> = _superVisionTimeout

    private val _pduSize: MutableLiveData<Int> = MutableLiveData(0)
    val pduSize: LiveData<Int> = _pduSize

    private val _mtuSize: MutableLiveData<Int> = MutableLiveData(0)
    val mtuSize: LiveData<Int> = _mtuSize

    private val _testInProgress: MutableLiveData<Boolean> = MutableLiveData(false)
    val testInProgress: LiveData<Boolean> = _testInProgress

    init {
        observeDeviceManager()
    }

    fun observeDeviceManager() {
        viewModelScope.launch {
            deviceManager.selectedDevice.collect {
                it?.let {
                    observeDevice(it)
                }
            }
        }
    }

    fun startScan() {
        deviceManager.startScan()
    }

    fun stopScan() {
        deviceManager.stopScan()
    }

    fun observeDevice(device: BGM220P){
        viewModelScope.launch {
            device.elapsedTime.collect {
                _elapsedTime.value = it
            }
        }

        viewModelScope.launch {
            device.bytesReceived.collect {
                _bytesReceived.value = it
            }
        }

        viewModelScope.launch {
            device.throughput.collect {
                _throughput.value = it
            }
        }

        viewModelScope.launch {
            device.phy.collect {
                _phy.value = it
            }
        }

        viewModelScope.launch {
            device.connectionInterval.collect {
                _connectionInterval.value = it
            }
        }

        viewModelScope.launch {
            device.latency.collect {
                _latency.value = it
            }
        }

        viewModelScope.launch {
            device.supervisionTimeout.collect {
                _superVisionTimeout.value = it
            }
        }

        viewModelScope.launch {
            device.pduSize.collect {
                _pduSize.value = it
            }
        }

        viewModelScope.launch {
            device.mtuSize.collect {
                _mtuSize.value = it
            }
        }

        viewModelScope.launch {
            device.testActive.collect {
                _testInProgress.value = it
            }
        }
    }

}