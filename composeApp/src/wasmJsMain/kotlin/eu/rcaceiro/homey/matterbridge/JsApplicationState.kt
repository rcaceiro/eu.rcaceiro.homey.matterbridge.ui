package eu.rcaceiro.homey.matterbridge

import kotlinx.coroutines.flow.MutableStateFlow

class JsApplicationState : ApplicationState {
    override val screen: MutableStateFlow<ApplicationState.Screen> =
        MutableStateFlow(ApplicationState.Screen.MainScreen)
}