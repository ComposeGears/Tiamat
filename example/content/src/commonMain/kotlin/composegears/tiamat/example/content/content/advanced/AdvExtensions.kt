package composegears.tiamat.example.content.content.advanced

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.*
import composegears.tiamat.example.ui.core.AppButton
import composegears.tiamat.example.ui.core.HSpacer
import composegears.tiamat.example.ui.core.Screen
import composegears.tiamat.example.ui.core.VSpacer

val AdvExtensions by navDestination<Unit> {
    Screen("Extensions") {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val nc = rememberNavController(
                key = "F&B",
                startDestination = AdvExtensionsScreen1,
                destinations = arrayOf(
                    AdvExtensionsScreen1,
                    AdvExtensionsScreen2,
                    AdvExtensionsScreen3,
                )
            )
            VSpacer()
            Text(
                buildString {
                    append("This is extensions sample.\n")
                    append("Last active screen reported by global extension: ${GlobalExtension.activeDestination}\n")
                    append("Current screen log message: ${nc.current?.ext<LocalExtension>()?.logMessage}\n")
                    append(
                        "Current screen extensions: ${
                            nc.current?.extensions?.joinToString(
                                ", ",
                                transform = { it::class.simpleName ?: "???" }
                            )
                        }"
                    )
                },
                textAlign = TextAlign.Center,
            )
            VSpacer()
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

// ------------ extensions -------------------

// having a dedicated object or class allows you cal ext fun to get extension ref
// eg: nc.current.ext<GlobalExtension>() -> return GlobalExtension or null if not attached
object GlobalExtension : Extension<Any?> {
    var activeDestination by mutableStateOf("")

    @Composable
    override fun NavDestinationScope<out Any?>.Content() {
        val entry = navEntry()
        LaunchedEffect(Unit) {
            activeDestination = entry.destination.name
        }
    }
}

class LocalExtension(val logMessage: String) : Extension<Any?> {
    @Composable
    override fun NavDestinationScope<out Any?>.Content() {
        LaunchedEffect(Unit) {
            println(logMessage)
        }
    }
}

// simple extensions, you can use them
// you will not be able to identify from the list of nav extensions
// type of ext will always be ExtensionImpl
val SimpleGlobalExtension = extension<Any> {}

// extension can also have its own UI placed over screen content
fun mySimpleExtensionBuilder(showOverlay: Boolean) = extension<Any> {
    if (showOverlay) Box(
        Modifier
            .fillMaxSize()
            .background(Color.Red.copy(alpha = 0.11f), RoundedCornerShape(8.dp)),
    ) {
        Text("Text from extension overlay", Modifier.align(Alignment.TopCenter))
        Text("Text from extension overlay", Modifier.align(Alignment.BottomCenter))
    }
}

// --------------- screens -------------------

private val AdvExtensionsScreen1 by navDestination<Unit>(
    GlobalExtension,
    LocalExtension("Screen1"),
    SimpleGlobalExtension
) {
    val nc = navController()
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 1", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            AppButton(
                "Next",
                endIcon = Icons.AutoMirrored.Default.KeyboardArrowRight,
                onClick = { nc.navigate(AdvExtensionsScreen2) }
            )
        }
    }
}

private val AdvExtensionsScreen2 by navDestination<Unit>(
    GlobalExtension,
    LocalExtension("Screen2"),
    mySimpleExtensionBuilder(showOverlay = true),
) {
    val nc = navController()
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 2", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            Row {
                AppButton(
                    "Back",
                    startIcon = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                    onClick = { nc.back() }
                )
                HSpacer()
                AppButton(
                    "Next",
                    endIcon = Icons.AutoMirrored.Default.KeyboardArrowRight,
                    onClick = { nc.navigate(AdvExtensionsScreen3) }
                )
            }
        }
    }
}

private val AdvExtensionsScreen3 by navDestination<Unit>(
    GlobalExtension,
    LocalExtension("Screen3"),
    extension { /* at place extension */ }
) {
    val nc = navController()
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 3", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            AppButton(
                "Back",
                startIcon = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                onClick = { nc.back() }
            )
        }
    }
}