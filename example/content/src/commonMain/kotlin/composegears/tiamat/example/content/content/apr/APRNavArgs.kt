package composegears.tiamat.example.content.content.apr

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.*
import composegears.tiamat.example.ui.core.AppButton
import composegears.tiamat.example.ui.core.Screen
import composegears.tiamat.example.ui.core.VSpacer

val APRNavArgs by navDestination<Unit> {
    Screen("NavArgs") {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val nc = rememberNavController(
                key = "NavArgs nav controller",
                startDestination = APRNavArgsScreen1,
                destinations = arrayOf(
                    APRNavArgsScreen1,
                    APRNavArgsScreen2,
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

private val APRNavArgsScreen1 by navDestination<Unit> {
    val nc = navController()
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // using rememberSaveable allow us to keep value during navigation
            var value by rememberSaveable { mutableIntStateOf(0) }
            Text("Screen 1", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            Text("`NavArgs` defines a data, required by upcoming screen")
            VSpacer()
            Text("Click button to pass selected value to next screen")
            VSpacer()
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { value-- }
                ) {
                    Icon(Icons.Default.Remove, "")
                }
                Text("Value: $value")
                IconButton(
                    onClick = { value++ }
                ) {
                    Icon(Icons.Default.Add, "")
                }
            }
            VSpacer()
            AppButton(
                "Next",
                endIcon = Icons.AutoMirrored.Default.KeyboardArrowRight,
                onClick = { nc.navigate(APRNavArgsScreen2, navArgs = value) }
            )
        }
    }
}

private val APRNavArgsScreen2 by navDestination<Int> {
    val nc = navController()
    val args = navArgsOrNull() // you can use `navArgs()` as unsafe option
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 2", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            Text("Value: $args")
            VSpacer()
            AppButton(
                "Back",
                startIcon = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                onClick = { nc.back() }
            )
        }
    }
}