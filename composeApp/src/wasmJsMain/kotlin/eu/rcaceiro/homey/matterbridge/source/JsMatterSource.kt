package eu.rcaceiro.homey.matterbridge.source

import eu.rcaceiro.homey.matterbridge.HomeyWasmBridge
import eu.rcaceiro.homey.matterbridge.JsBridge
import eu.rcaceiro.homey.matterbridge.model.Bridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asDeferred
import kotlinx.coroutines.withContext

class JsMatterSource : MatterSource {
    override suspend fun get(): Bridge =
        withContext(Dispatchers.Default) {
            val jsBridge = HomeyWasmBridge
                .matterService
                .get()
                .asDeferred<JsBridge>()
                .await()

            return@withContext Bridge(
                qrcode = jsBridge.qrCode,
                passcode = jsBridge.passcode
            )
        }
}