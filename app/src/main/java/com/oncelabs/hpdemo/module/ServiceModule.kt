package com.oncelabs.hpdemo.module

import com.oncelabs.hpdemo.manager.DeviceManager
import com.oncelabs.hpdemo.manager.implementation.DeviceManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {
    @Binds
    abstract fun provideDeviceManager(impl: DeviceManagerImpl): DeviceManager
}