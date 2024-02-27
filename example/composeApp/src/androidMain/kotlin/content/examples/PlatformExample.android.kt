package content.examples

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.NavDestination
import com.composegears.tiamat.Navigation
import com.composegears.tiamat.navDestination
import com.composegears.tiamat.rememberNavController
import content.examples.platform.AndroidViewLifecycleScreen
import content.examples.platform.MainPlatformScreen
import content.examples.platform.SavedStateScreen

actual val PlatformExample: NavDestination<Unit> by navDestination {
    val platformNavController = rememberNavController(
        key = "platformNavController",
        startDestination = MainPlatformScreen,
        destinations = arrayOf(MainPlatformScreen, SavedStateScreen, AndroidViewLifecycleScreen)
    )
    val titleVisible by remember {
        derivedStateOf { platformNavController.current == MainPlatformScreen }
    }

    Surface {
        Column(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(visible = titleVisible) {
                Surface(shadowElevation = 8.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(platformNavController::back) {
                            Icon(Icons.Default.ArrowBack, "")
                        }
                        Text(
                            text = "Platform samples",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Navigation(
                    navController = platformNavController,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}