package composegears.tiamat.example.content.content.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.composegears.tiamat.navDestination
import composegears.tiamat.example.ui.core.Screen

val NavForwardAndBack by navDestination<Unit> {
    Screen("Forward & back") {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("TODO")
        }
    }
}