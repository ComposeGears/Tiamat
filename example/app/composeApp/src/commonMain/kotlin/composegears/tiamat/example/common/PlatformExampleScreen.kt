package composegears.tiamat.example.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.NavDestination
import com.composegears.tiamat.navController
import com.composegears.tiamat.navDestination
import composegears.tiamat.example.ui.core.SimpleScreen

val PlatformScreen by navDestination<Unit> {
    val navController = navController()

    SimpleScreen("Platform ${platformConfig.platformName}") {
        LazyColumn(
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(platformConfig.availableScreens) { (name, screen) ->
                Button(
                    modifier = Modifier.widthIn(max = 450.dp),
                    onClick = {
                        navController.navigate(screen)
                    },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(modifier = Modifier.weight(1f), text = name)
                        Icon(Icons.AutoMirrored.Filled.NavigateNext, "")
                    }
                }
            }
        }
    }
}

expect val PlatformExampleScreen: NavDestination<Unit>
expect val platformConfig: PlatformConfig

class PlatformConfig(
    val platformName: String,
    val availableScreens: List<PlatformDestination>,
) {
    data class PlatformDestination(
        val name: String,
        val destination: NavDestination<*>
    )

    fun destinations() = availableScreens
        .map { it.destination }
        .toTypedArray()
}
