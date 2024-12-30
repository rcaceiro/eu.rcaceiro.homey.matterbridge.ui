package eu.rcaceiro.homey.matterbridge.viewmodels

import androidx.compose.runtime.Immutable
import eu.rcaceiro.homey.matterbridge.ApplicationManager
import eu.rcaceiro.homey.matterbridge.ApplicationState
import eu.rcaceiro.homey.matterbridge.model.Bridge
import eu.rcaceiro.homey.matterbridge.source.MatterSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainScreenViewModel(
    private val application: ApplicationState,
    private val scope: CoroutineScope,
    private val source: MatterSource,
) {
    private val mState = MutableStateFlow<State>(State.Loading)
    val state: SharedFlow<State> = this.mState.asStateFlow()

    init {
        this.scope.launch(Dispatchers.Default) {
            val bridge = this@MainScreenViewModel.source.get()
            val event = State.QrCode(bridge)
            this@MainScreenViewModel.mState.value = event
        }
    }

    constructor(manager: ApplicationManager) : this(
        application = manager.state,
        source = manager.matterSource,
        scope = manager.scope,
    )

    fun onEvent(event: Event) {
        this.scope.launch(Dispatchers.Default) {
        }
    }

    sealed interface State {
        @Immutable
        data class QrCode(val bridge: Bridge) : State

        data object Loading : State
    }

    sealed interface Event {
    }
}