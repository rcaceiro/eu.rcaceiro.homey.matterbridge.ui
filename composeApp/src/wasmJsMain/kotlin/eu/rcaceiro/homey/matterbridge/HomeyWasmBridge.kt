package eu.rcaceiro.homey.matterbridge

import kotlin.js.Promise

external interface DeviceService {
    fun getAll(): Promise<JsArray<JsDevice>>
}

external interface MatterService {
    fun get(): Promise<JsBridge>
}

external object HomeyWasmBridge {
    @JsName("device_service")
    val deviceService: DeviceService

    @JsName("is_light_mode_on")
    val isLightModeOn: Boolean

    @JsName("matter_service")
    val matterService: MatterService

    val url: String

    fun map(): MapInterop
    fun translate(key: String, tokens: JsAny?): String
}

external interface JsBridge : JsAny {
    @JsName("qr_code")
    val qrCode: String
    val passcode: String
}

external interface JsDevice : JsAny {
    val id: String
    val icon: String
    val name: String
    val room: String
}

external class MapInterop : JsAny {
    fun add(key: String, value: String)
}