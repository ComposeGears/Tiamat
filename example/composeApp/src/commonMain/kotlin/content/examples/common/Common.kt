package content.examples.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.NavDestinationScope
import com.composegears.tiamat.navController

@Composable
fun NavDestinationScope<*>.SimpleScreen(
    title: String,
    color: Color = MaterialTheme.colors.surface,
    body: @Composable BoxScope.() -> Unit
) {
    val navController = navController()
    Surface(color = color) {
        Column(Modifier.fillMaxSize()) {
            Surface(
                color = MaterialTheme.colors.primarySurface,
                elevation = 8.dp
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(navController::back) {
                        Icon(Icons.Default.ArrowBack, "")
                    }
                    Text(
                        text = title,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                body()
            }
        }
    }
}