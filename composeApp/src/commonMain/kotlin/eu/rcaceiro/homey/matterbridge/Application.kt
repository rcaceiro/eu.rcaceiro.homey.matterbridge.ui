package eu.rcaceiro.homey.matterbridge

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import eu.rcaceiro.homey.matterbridge.screens.MainScreen
import eu.rcaceiro.homey.matterbridge.viewmodels.MainScreenViewModel

@Composable
fun Application(application: ApplicationManager) {
    val colors = remember(application.isLightModeOn) {
        if (application.isLightModeOn) {
            lightColorScheme()
        } else {
            darkColorScheme()
        }
    }
    MaterialTheme(colorScheme = colors) {
        val screen by application.state.screen.collectAsState()
        NavScreen(
            application = application,
            screen = screen,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun NavScreen(
    application: ApplicationManager,
    screen: ApplicationState.Screen,
    modifier: Modifier = Modifier,
) {
    SharedTransitionLayout(modifier = modifier) {
        AnimatedContent(
            targetState = screen,
            modifier = Modifier.fillMaxSize(),
        ) {
            when (it) {
                is ApplicationState.Screen.MainScreen -> {
                    val viewmodel = remember(application) {
                        MainScreenViewModel(application)
                    }
                    val state by viewmodel.state.collectAsState(initial = MainScreenViewModel.State.Loading)
                    MainScreen(
                        onEvent = viewmodel::onEvent,
                        state = state,
                        translate = application::translate,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}