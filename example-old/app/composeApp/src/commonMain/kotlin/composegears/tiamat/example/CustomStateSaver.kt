package composegears.tiamat.example

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.Navigation
import com.composegears.tiamat.StorageMode
import com.composegears.tiamat.navDestination
import com.composegears.tiamat.rememberNavController
import composegears.tiamat.example.ui.core.SimpleScreen
import composegears.tiamat.example.ui.core.TextBody
import composegears.tiamat.example.ui.core.TextButton
import composegears.tiamat.example.ui.core.webPathExtension

val CustomStateSaverRoot by navDestination<Unit>(webPathExtension()) {
    SimpleScreen("Custom state saver") {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            var savedState by remember { mutableStateOf<Map<String, Any?>>(emptyMap()) }
            var showNavController by remember { mutableStateOf(true) }

            TextButton("Toggle navigation -> ${if (showNavController) "Hide" else "Show"}") {
                showNavController = !showNavController
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp)
                    .border(4.dp, MaterialTheme.colorScheme.onSurface)
                    .padding(4.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (showNavController) {
                    val savableNavController = rememberNavController(
                        storageMode = StorageMode.Memory,
                        startDestination = DataPassingParamsRoot,
                        destinations = arrayOf(
                            DataPassingParamsRoot,
                            DataPassingParamsScreen,
                        )
                    ) {
                        // restore state from custom storage
                        if (savedState.isNotEmpty())
                            loadFromSavedState(savedState)
                    }
                    Navigation(navController = savableNavController)
                    DisposableEffect(Unit) {
                        onDispose {
                            // save state into custom storage
                            savedState = savableNavController.getSavedState()
                        }
                    }
                } else TextBody("Nothing")
            }
        }
    }
}