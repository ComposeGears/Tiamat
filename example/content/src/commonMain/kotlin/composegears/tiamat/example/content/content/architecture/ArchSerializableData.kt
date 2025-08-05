package composegears.tiamat.example.content.content.architecture

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.compose.*
import com.composegears.tiamat.navigation.SavedState
import com.composegears.tiamat.navigation.serializable
import com.composegears.tiamat.toHumanReadableString
import composegears.tiamat.example.ui.core.AppButton
import composegears.tiamat.example.ui.core.Screen
import composegears.tiamat.example.ui.core.ScreenInfo
import composegears.tiamat.example.ui.core.VSpacer
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

val ArchSerializableData by navDestination<Unit>(ScreenInfo()) {
    Screen("Serializable Data") {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            VSpacer()
            var ncSavedState by remember { mutableStateOf<SavedState?>(null) }
            var showNavigation by remember { mutableStateOf(true) }
            var serializableNavArgs by remember { mutableStateOf(true) }
            var serializableFreeArgs by remember { mutableStateOf(true) }
            AnimatedContent(showNavigation, contentAlignment = Alignment.Center) {
                if (it) AppButton("Save / Serialize", onClick = { showNavigation = false })
                else AppButton("Restore / Deserialize", onClick = { showNavigation = true })
            }
            if (showNavigation) {
                VSpacer()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(serializableNavArgs, onCheckedChange = { serializableNavArgs = it })
                    Text("Serializable NavArgs")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(serializableFreeArgs, onCheckedChange = { serializableFreeArgs = it })
                    Text("Serializable FreeArgs")
                }
                VSpacer()
                val nc = rememberNavController(
                    //key = "Arch custom save state nav controller",
                    startEntry = null,
                    savedState = ncSavedState,
                )
                var shouldFlush by remember { mutableStateOf(nc.getCurrentNavEntry() == null) }
                Navigation(
                    navController = nc,
                    destinations = arrayOf(ArchSerializableDataScreen),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                )
                LaunchedEffect(serializableNavArgs, serializableFreeArgs) {
                    if (shouldFlush) when {
                        serializableNavArgs && serializableFreeArgs -> nc.replace(
                            entry = ArchSerializableDataScreen,
                            navArgs = serializable(ArchSerializableDataClass(1)),
                            freeArgs = serializable(ArchSerializableDataClass(2)),
                        )
                        serializableNavArgs && !serializableFreeArgs -> nc.replace(
                            entry = ArchSerializableDataScreen,
                            navArgs = ArchSerializableDataClass(1).serializable(),
                            freeArgs = ArchSerializableDataClass(2),
                        )
                        !serializableNavArgs && serializableFreeArgs -> nc.replace(
                            entry = ArchSerializableDataScreen,
                            navArgs = ArchSerializableDataClass(1),
                            freeArgs = ArchSerializableDataClass(2).serializable(),
                        )
                        !serializableNavArgs && !serializableFreeArgs -> nc.replace(
                            entry = ArchSerializableDataScreen,
                            navArgs = ArchSerializableDataClass(1),
                            freeArgs = ArchSerializableDataClass(2),
                        )
                    } else shouldFlush = true
                }
                DisposableEffect(Unit) {
                    onDispose {
                        ncSavedState = nc.saveToSavedState()
                    }
                }
            } else {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                ) {
                    Text(
                        "Nav controller state:\n\n${ncSavedState?.toHumanReadableString()}",
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Serializable
data class ArchSerializableDataClass(val t: Int)

@OptIn(InternalSerializationApi::class)
private val ArchSerializableDataScreen by navDestination<ArchSerializableDataClass> {
    val navArgs = navArgs()
    val freeArgs1 = freeArgs<Any>()
    val freeArgs2 = freeArgs<Int>()
    val freeArgs3 = freeArgs<ArchSerializableDataClass>()
    val freeArgs4 = freeArgs<Any>()
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Data Screen", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            Text(
                text = "decoded (if serializable) automatically as the type is already defined in the destination",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "NavArgs = $navArgs",
                style = MaterialTheme.typography.bodyLarge
            )
            VSpacer()
            Text(
                text = "!! IMPORTANT !!",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Return either data class (if not serialized or not serializable)",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "or null (if serialized) as it was not deserialized yet",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "freeArgs1<Any> = $freeArgs1",
                style = MaterialTheme.typography.bodyLarge
            )
            VSpacer()
            Text(
                text = "Return null as it will try to cast to Int and fails in both cases (serialized or not)",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "freeArgs1<Int> = $freeArgs2",
                style = MaterialTheme.typography.bodyLarge
            )
            VSpacer()
            Text(
                text = "Return data class, as it is already this type (if not serializable)",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "or else (serializable) it was deserialized successfully",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "freeArgs1<ArchSerializableDataClass> = $freeArgs3",
                style = MaterialTheme.typography.bodyLarge
            )
            VSpacer()
            Text(
                text = "Return data class as it present as it is (if not serializable)",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "or else (serializable) it was deserialized in previous step",
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = "freeArgs1<Any> = $freeArgs4",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}