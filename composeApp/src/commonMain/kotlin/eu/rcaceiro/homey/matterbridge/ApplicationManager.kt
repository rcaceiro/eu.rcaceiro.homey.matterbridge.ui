package eu.rcaceiro.homey.matterbridge

import eu.rcaceiro.homey.matterbridge.source.DeviceSource
import eu.rcaceiro.homey.matterbridge.source.MatterSource
import kotlinx.coroutines.CoroutineScope

interface ApplicationManager {
    val deviceSource: DeviceSource
    val isLightModeOn: Boolean
    val matterSource: MatterSource
    val scope: CoroutineScope
    val state: ApplicationState
    fun translate(resource: String, args: Map<String, String>?): String
}