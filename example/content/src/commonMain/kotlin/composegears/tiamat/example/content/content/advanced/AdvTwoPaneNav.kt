package composegears.tiamat.example.content.content.advanced

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.Navigation
import com.composegears.tiamat.navDestination
import com.composegears.tiamat.rememberNavController
import composegears.tiamat.example.ui.core.Screen

// TODO dodo
val AdvTwoPaneNav by navDestination<Unit> {
    Screen("Two Pane navigation") {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            // val isWideScreen by remember { derivedStateOf { maxWidth > 600.dp } }
            val nc = rememberNavController(
                key = "Two pane nav controller",
                startDestination = AdvTwoPaneNavList,
                destinations = arrayOf(
                    AdvTwoPaneNavList,
                    AdvTwoPaneNavDetails,
                    AdvTwoPaneNavDeepDetails,
                )
            )
            Navigation(
                nc,
                Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            )
        }
    }
}

val AdvTwoPaneNavList by navDestination<Unit> {
}

val AdvTwoPaneNavDetails by navDestination<Unit> {
}

val AdvTwoPaneNavDeepDetails by navDestination<Unit> {
}