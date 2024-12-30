package eu.rcaceiro.homey.matterbridge

import kotlinx.coroutines.flow.MutableStateFlow

interface ApplicationState {
    val screen: MutableStateFlow<Screen>

    sealed interface Screen {
        data object MainScreen : Screen
    }
}