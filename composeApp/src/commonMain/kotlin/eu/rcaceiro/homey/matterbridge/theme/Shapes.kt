package eu.rcaceiro.homey.matterbridge.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import qrgenerator.qrkitpainter.QrKitBallShape
import qrgenerator.qrkitpainter.QrKitFrameShape
import qrgenerator.qrkitpainter.QrKitPixelShape
import qrgenerator.qrkitpainter.QrKitShapes
import qrgenerator.qrkitpainter.createCircle
import qrgenerator.qrkitpainter.createRoundCorners

@Composable
fun rememberQrKitShapes(): QrKitShapes {
    return remember {
        val pixelShape = QrKitPixelShape.createRoundCorners()
        QrKitShapes(
            darkPixelShape = pixelShape,
            lightPixelShape = pixelShape,
            ballShape = QrKitBallShape.Companion.createCircle(),
            frameShape = QrKitFrameShape.Companion.createRoundCorners(0.1f),
        )
    }
}