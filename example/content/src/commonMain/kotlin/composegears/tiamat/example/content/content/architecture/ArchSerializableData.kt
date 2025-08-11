package composegears.tiamat.example.content.content.architecture

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.compose.*
import com.composegears.tiamat.navigation.NavData
import com.composegears.tiamat.navigation.SavedState
import com.composegears.tiamat.toHumanReadableString
import composegears.tiamat.example.ui.core.*
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

val ArchSerializableData by navDestination<Unit>(ScreenInfo()) {
    Screen("Serializable Data") {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            VSpacer()
            var ncSavedState by remember { mutableStateOf<SavedState?>(null) }
            var showNavigation by remember { mutableStateOf(true) }
            AnimatedContent(showNavigation, contentAlignment = Alignment.Center) {
                if (it) AppButton("Save / Serialize", onClick = { showNavigation = false })
                else AppButton("Restore / Deserialize", onClick = { showNavigation = true })
            }
            if (showNavigation) {
                val nc = rememberNavController(
                    //key = "Arch custom save state nav controller",
                    startEntry = null,
                    savedState = ncSavedState,
                )
                VSpacer()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    fun replace(arg: Any) {
                        nc.replace(
                            entry = ArchSerializableDataScreen,
                            navArgs = ArchSerializableDataClass(1),
                            freeArgs = arg,
                        )
                    }
                    AppButton("FreeArgs:Int", { replace(1) })
                    HSpacer()
                    AppButton("FreeArgs:String", { replace("string") })
                    HSpacer()
                    AppButton("FreeArgs:Data", { replace(ArchSimpleDataClass(1)) })
                    HSpacer()
                    AppButton("FreeArgs:Serializable", { replace(ArchSerializableDataClass(1)) })
                }
                VSpacer()
                Navigation(
                    navController = nc,
                    destinations = arrayOf(ArchSerializableDataScreen),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                )
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

data class ArchSimpleDataClass(val t: Int)

@Serializable
data class ArchSerializableDataClass(val t: Int) : NavData

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
                text = "Return either data (if not serialized or not serializable)",
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
                text = "Attempt to display as Int",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "freeArgs2<Int> = $freeArgs2",
                style = MaterialTheme.typography.bodyLarge
            )
            VSpacer()
            Text(
                text = "Attempting to deserialize as ArchSerializableDataClass",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "It will be either data or null if type not match",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "freeArgs3<ArchSerializableDataClass> = $freeArgs3",
                style = MaterialTheme.typography.bodyLarge
            )
            VSpacer()
            Text(
                text = "Return data (if it was not serialized)",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "or the instance of ArchSerializableDataClass",
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = "as it was deserialized at the step before",
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = "freeArgs4<Any> = $freeArgs4",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}