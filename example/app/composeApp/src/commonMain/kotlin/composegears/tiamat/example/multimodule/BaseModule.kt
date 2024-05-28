package composegears.tiamat.example.multimodule

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.staticCompositionLocalOf

internal val LocalSignalsHandler = staticCompositionLocalOf { Signals() }

/**
 * Simple signal/broadcast implementation
 */
class Signals internal constructor() {

    private val handlers = ArrayList<(signal: Any?) -> Boolean>()

    internal fun add(handler: (signal: Any?) -> Boolean) {
        handlers.add(handler)
    }

    internal fun remove(handler: (signal: Any?) -> Boolean) {
        handlers.remove(handler)
    }

    fun send(data: Any?) {
        var index = handlers.lastIndex
        while (index >= 0) {
            if (handlers[index--](data)) return
        }
    }
}

/**
 * signal/broadcast observer
 */
@Composable
fun SignalEffect(handler: (signal: Any?) -> Boolean) {
    val signals = LocalSignalsHandler.current
    DisposableEffect(handler) {
        signals.add(handler)
        onDispose {
            signals.remove(handler)
        }
    }
}

/**
 * signal/broadcast send-helper
 */
@Composable
fun rememberSignals(): Signals = LocalSignalsHandler.current

/**
 * Known/defined signals, shared across all modules
 */
sealed interface KnownSignals {
    data object ExitFlow : KnownSignals
    data object ReopenFeature1 : KnownSignals
    data class ShowMessage(val message: String) : KnownSignals
}