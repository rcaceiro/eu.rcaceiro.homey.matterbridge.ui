package eu.rcaceiro.homey.matterbridge.source

import eu.rcaceiro.homey.matterbridge.model.Bridge

interface MatterSource {
    suspend fun get(): Bridge
}