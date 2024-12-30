package eu.rcaceiro.homey.matterbridge.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import qrgenerator.qrkitpainter.QrKitBrush
import qrgenerator.qrkitpainter.QrKitColors
import qrgenerator.qrkitpainter.solidBrush

@Composable
fun rememberQrKitColors(): QrKitColors {
    val colorScheme = MaterialTheme.colorScheme
    return remember {
        val brush = QrKitBrush.solidBrush(colorScheme.onBackground)
        QrKitColors(
            darkBrush = brush,
            ballBrush = brush,
            frameBrush = brush,
        )
    }
}