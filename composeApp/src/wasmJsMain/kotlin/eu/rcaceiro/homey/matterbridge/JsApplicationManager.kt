package eu.rcaceiro.homey.matterbridge

import eu.rcaceiro.homey.matterbridge.source.DeviceSource
import eu.rcaceiro.homey.matterbridge.source.JsDeviceSource
import eu.rcaceiro.homey.matterbridge.source.JsMatterSource
import eu.rcaceiro.homey.matterbridge.source.MatterSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object JsApplicationManager : ApplicationManager {
    override val deviceSource: DeviceSource = JsDeviceSource()
    override val isLightModeOn: Boolean = HomeyWasmBridge.isLightModeOn
    override val matterSource: MatterSource = JsMatterSource()
    override val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    override val state: ApplicationState = JsApplicationState()
    override fun translate(resource: String, args: Map<String, String>?): String {
        val map = args?.let {
            val map = HomeyWasmBridge.map()
            it.forEach { (key, value) ->
                map.add(key, value)
            }
            return@let map
        }
        return HomeyWasmBridge.translate(resource, map)
    }
}