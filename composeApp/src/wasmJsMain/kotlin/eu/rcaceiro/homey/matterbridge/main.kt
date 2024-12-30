package eu.rcaceiro.homey.matterbridge

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    SingletonImageLoader.setSafe { context ->
        ImageLoader.Builder(context)
            .components {
                this.add(KtorNetworkFetcherFactory())
                this.add(SvgDecoder.Factory(renderToBitmap = false))
            }
            .crossfade(enable = true)
            .build()
    }

    ComposeViewport(document.body!!) {
        Application(application = JsApplicationManager)
    }
}