package composegears.tiamat.example.ui.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
    color: Color = MaterialTheme.colorScheme.surface,
    body: @Composable BoxScope.() -> Unit
) {
    val navController = navController()
    Surface(color = color) {
        Column(Modifier.fillMaxSize()) {
            Surface(shadowElevation = 8.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(navController::back) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "")
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