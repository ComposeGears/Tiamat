package content.examples.platform

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.navController
import com.composegears.tiamat.navDestination

val MainPlatformScreen by navDestination<Unit> {
    val navController = navController()

    val content = remember {
        listOf(
            "Android SavedState" to { navController.navigate(SavedStateScreen) },
            "AndroidView + Lifecycle handle" to { navController.navigate(AndroidViewLifecycleScreen) },
        )
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(content) { (name, action) ->
                Button(
                    modifier = Modifier.widthIn(max = 450.dp),
                    onClick = action,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(modifier = Modifier.weight(1f), text = name)
                        Icon(Icons.Default.NavigateNext, "")
                    }
                }
            }
        }
    }
}