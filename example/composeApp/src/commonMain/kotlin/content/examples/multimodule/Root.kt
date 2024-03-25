package content.examples.multimodule

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.Navigation
import com.composegears.tiamat.navController
import com.composegears.tiamat.navDestination
import com.composegears.tiamat.rememberNavController
import content.examples.common.SimpleScreen

// Root - knows about Feature1 and Feature2
// Feature1 - knows about Feature2.
// Feature2 - may ask Root to do smth. !!!(do not know about Root, neither Feature1)
// All modules - knows about BaseModule.

val MultiModuleRoot by navDestination<Unit> {
    SimpleScreen("Multi-module root") {
        val nc = navController()
        val multiModuleNavController = rememberNavController(
            key = "multiModuleNavController",
            startDestination = MultiModuleFeature1,
            destinations = arrayOf(MultiModuleFeature1, MultiModuleFeature2)
        )
        var lastSignal by remember { mutableStateOf("none") }
        SignalEffect {
            when (it) {
                is KnownSignals.ExitFlow -> {
                    lastSignal = it.toString()
                    nc.back()
                    true
                }
                is KnownSignals.ReopenFeature1 -> {
                    lastSignal = it.toString()

                    multiModuleNavController.editBackStack { clear() }
                    multiModuleNavController.replace(MultiModuleFeature1)
                    true
                }
                is KnownSignals.ShowMessage -> {
                    lastSignal = it.toString()
                    true
                }
                else -> false
            }
        }
        Column {
            Text(
                "Last signal: $lastSignal",
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            HorizontalDivider()
            Navigation(multiModuleNavController, Modifier.fillMaxSize())
        }
    }
}