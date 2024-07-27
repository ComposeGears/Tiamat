package composegears.tiamat.example.multimodule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.composegears.tiamat.navDestination
import composegears.tiamat.example.ui.core.TextButton
import kotlin.random.Random

// Feature2 only know about BaseModule

val MultiModuleFeature2 by navDestination<Unit> {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Multi-module feature2")
        val signals = rememberSignals()
        Column {
            TextButton("Send message") {
                val message = "Rnd: " + Random.nextInt()
                signals.send(KnownSignals.ShowMessage(message))
            }
            TextButton("Open feature1") {
                signals.send(KnownSignals.ReopenFeature1)
            }
            TextButton("Exit flow") {
                signals.send(KnownSignals.ExitFlow)
            }
        }
    }
}
