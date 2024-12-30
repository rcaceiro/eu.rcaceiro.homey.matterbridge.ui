package eu.rcaceiro.homey.matterbridge.model

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class Device @OptIn(ExperimentalUuidApi::class) constructor(
    val id: Uuid,
    val icon: String,
    val name: String,
    val room: String,
)
