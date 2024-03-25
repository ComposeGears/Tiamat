package content.examples.multimodule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.composegears.tiamat.navController
import com.composegears.tiamat.navDestination
import content.examples.common.NextButton

// Feature1 know about Feature2

val MultiModuleFeature1 by navDestination<Unit> {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val nc = navController()
        Text("Multi-module feature1")
        NextButton("Open feature 2") {
            nc.navigate(MultiModuleFeature2)
        }
    }
}