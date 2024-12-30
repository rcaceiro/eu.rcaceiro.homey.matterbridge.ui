package eu.rcaceiro.homey.matterbridge.source

import eu.rcaceiro.homey.matterbridge.model.Device

interface DeviceSource {
    suspend fun getAll(): List<Device>
}