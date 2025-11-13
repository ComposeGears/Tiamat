package composegears.tiamat.sample.content.advanced

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.compose.*
import composegears.tiamat.sample.icons.Icons
import composegears.tiamat.sample.icons.KeyboardArrowLeft
import composegears.tiamat.sample.icons.KeyboardArrowRight
import composegears.tiamat.sample.ui.*

val AdvExtensions by navDestination(ScreenInfo()) {
    Screen("Extensions") {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val nc = rememberNavController(
                key = "Extensions nav controller",
                startDestination = AdvExtensionsScreen1,
            )
            val currentNavDestination by nc.currentNavDestinationAsState()
            VSpacer()
            Text(
                text = buildString {
                    append("This is extensions sample.\n")
                    append("—————\n")
                    append("Last active screen reported by global extension: ${GlobalExtension.activeDestination}\n")
                    append("—————\n")
                    append("Current screen log message: ${currentNavDestination?.ext<LocalExtension>()?.logMessage}\n")
                    append("—————\n")
                    append(
                        "Current screen extensions: ${
                            currentNavDestination?.extensions()?.joinToString(
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
                navController = nc,
                destinations = arrayOf(
                    AdvExtensionsScreen1,
                    AdvExtensionsScreen2,
                    AdvExtensionsScreen3,
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            )
        }
    }
}

// ------------ extensions -------------------

// Marker/Simple ext have no content and may be a marker/data handler to be used
// in tandem with other extensions
class MarkerExtension(val data: String) : NavExtension<Any?>

// having a dedicated object or class allows you cal ext fun to get extension ref
// eg: nc.current.ext<GlobalExtension>() -> return GlobalExtension or null if not attached
object GlobalExtension : ContentExtension<Any> {
    var activeDestination by mutableStateOf("")

    @Composable
    override fun NavDestinationScope<out Any>.Content() {
        val entry = navEntry()
        LaunchedEffect(Unit) {
            activeDestination = entry.destination.name
        }
    }

    // optional override, default type is Overlay,
    // Underlay means that composable content of the ext will be placed before destination content
    override fun getType() = ContentExtension.Type.Underlay
}

class LocalExtension(val logMessage: String) : ContentExtension<Any> {
    @Composable
    override fun NavDestinationScope<out Any>.Content() {
        LaunchedEffect(Unit) {
            println(logMessage)
        }
    }
}

// simple extensions, you can use them
// you will not be able to identify from the list of nav extensions
// type of ext will always be ContentExtensionImpl
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

private val AdvExtensionsScreen1 by navDestination(
    GlobalExtension,
    LocalExtension("Screen1"),
    SimpleGlobalExtension
) {
    val nc = navController()
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 1", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            AppButton(
                "Next",
                endIcon = Icons.KeyboardArrowRight,
                onClick = { nc.navigate(AdvExtensionsScreen2) }
            )
        }
    }
}

private val AdvExtensionsScreen2 by navDestination(
    GlobalExtension,
    LocalExtension("Screen2"),
    mySimpleExtensionBuilder(showOverlay = true),
) {
    val nc = navController()
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 2", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            Row {
                AppButton(
                    "Back",
                    startIcon = Icons.KeyboardArrowLeft,
                    onClick = { nc.back() }
                )
                HSpacer()
                AppButton(
                    "Next",
                    endIcon = Icons.KeyboardArrowRight,
                    onClick = { nc.navigate(AdvExtensionsScreen3) }
                )
            }
        }
    }
}

private val AdvExtensionsScreen3 by navDestination(
    GlobalExtension,
    LocalExtension("Screen3"),
    MarkerExtension("Some data")
) {
    val nc = navController()
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 3", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            AppButton(
                "Back",
                startIcon = Icons.KeyboardArrowLeft,
                onClick = { nc.back() }
            )
        }
    }
}

@Preview
@Composable
private fun AdvExtensionsPreview() = AppTheme {
    TiamatPreview(destination = AdvExtensions)
}
