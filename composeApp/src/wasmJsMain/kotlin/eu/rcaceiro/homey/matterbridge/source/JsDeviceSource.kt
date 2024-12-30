package eu.rcaceiro.homey.matterbridge.source

import eu.rcaceiro.homey.matterbridge.HomeyWasmBridge
import eu.rcaceiro.homey.matterbridge.JsDevice
import eu.rcaceiro.homey.matterbridge.model.Device
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

class JsDeviceSource : DeviceSource {
    @OptIn(ExperimentalUuidApi::class)
    override suspend fun getAll(): List<Device> =
        withContext(Dispatchers.Default) {
            val jsDevices = HomeyWasmBridge
                .deviceService
                .getAll()
                .asDeferred<JsArray<JsDevice>>()
                .await()

            val devices = ArrayList<Deferred<Device?>>()
            repeat(jsDevices.length) {
                val deferred = this.async(Dispatchers.Default) {
                    val jsDevice = jsDevices[it]
                        ?: return@async null

                    return@async Device(
                        id = Uuid.parse(jsDevice.id),
                        name = jsDevice.name,
                        icon = "${HomeyWasmBridge.url}${jsDevice.icon}",
                        room = jsDevice.room,
                    )
                }
                devices.add(deferred)
            }
            devices.awaitAll().mapNotNull { it }
        }
}