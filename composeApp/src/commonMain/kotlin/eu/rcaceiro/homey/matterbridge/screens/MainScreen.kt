package eu.rcaceiro.homey.matterbridge.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.rcaceiro.homey.matterbridge.model.Bridge
import eu.rcaceiro.homey.matterbridge.theme.rememberQrKitColors
import eu.rcaceiro.homey.matterbridge.theme.rememberQrKitShapes
import eu.rcaceiro.homey.matterbridge.viewmodels.MainScreenViewModel
import qrgenerator.qrkitpainter.rememberQrKitPainter

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MainScreen(
    state: MainScreenViewModel.State,
    modifier: Modifier = Modifier,
    translate: suspend (String, args: Map<String, String>?) -> String,
    onEvent: (MainScreenViewModel.Event) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
    ) {
        this.item {
            CardQrCode(
                state = state,
                translate = translate,
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CardQrCode(
    state: MainScreenViewModel.State,
    modifier: Modifier = Modifier,
    translate: suspend (String, args: Map<String, String>?) -> String,
) {
    Card(modifier = modifier) {
        AnimatedContent(
            targetState = state,
            modifier = Modifier.fillMaxSize(),
        ) {
            when (it) {
                is MainScreenViewModel.State.Loading ->
                    Loading(modifier = Modifier.padding(16.dp))

                is MainScreenViewModel.State.QrCode ->
                    QrCode(
                        bridge = it.bridge,
                        translate = translate,
                        modifier = Modifier.padding(16.dp)
                    )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun QrCode(
    bridge: Bridge,
    modifier: Modifier = Modifier,
    translate: suspend (String, args: Map<String, String>?) -> String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        val qrCodeContentDescription by produceState("") {
            translate("content_description.qr_code", null)
        }
        val qrColors = rememberQrKitColors()
        val qrShapes = rememberQrKitShapes()
        val qrCode = rememberQrKitPainter(bridge.qrcode) {
            this.colors = qrColors
            this.shapes = qrShapes
        }
        Image(
            painter = qrCode,
            contentDescription = qrCodeContentDescription,
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
        )
        Text(
            text = bridge.passcode,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 8.dp),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Loading(
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        CircularProgressIndicator(
            modifier = modifier.align(Alignment.Center),
        )
    }
}