package eu.rcaceiro.homey.matterbridge.model

import kotlin.uuid.ExperimentalUuidApi

data class Bridge @OptIn(ExperimentalUuidApi::class) constructor(
    val qrcode: String,
    val passcode: String,
)
