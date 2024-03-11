package content.examples.platform.examples

import android.os.Parcelable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import content.examples.common.*
import kotlinx.parcelize.Parcelize

val SavedStateScreen by navDestination<Unit> {
    SimpleScreen("Android SavedState") {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            TextBody("Savable nav example")
            Column {
                TextCaption("In order to test this behaviour (IMPORTANT):")
                TextCaption("• go ./example/composeApp/src/commonMain/kotlin/App.kt")
                TextCaption("• change storageMode to StorageMode.Savable")
                TextCaption("• !!WARNING!! other screens may not work due to this changes!!")
                TextCaption("• compile android app")
                TextCaption("• go to developer settings of your device")
                TextCaption("• find `dont keep activities` option and turn it on")
                TextCaption("• open next screens")
                TextCaption("• hide and then unhide app")
                TextCaption("• observer data restored from saveState")
            }
            Spacer(24.dp)
            TextBody("Only bundleable data supported:")
            TextCaption("eg: primitives, parcelable (see: SaveableStateRegistry.canBeSaved)")
            val savableNavController = rememberNavController(
                key = "savableNavController",
                storageMode = StorageMode.Savable,
                startDestination = SavableDataExampleScreenRoot,
                destinations = arrayOf(
                    SavableDataExampleScreenRoot,
                    SavableDataExampleArgsScreen,
                )
            )
            Navigation(
                savableNavController,
                Modifier
                    .padding(16.dp)
                    .border(4.dp, MaterialTheme.colorScheme.onSurface)
                    .padding(4.dp)
            )
        }
    }
}

@Parcelize
class ParcelableNavArgs(val count: Int) : Parcelable

val SavableDataExampleScreenRoot by navDestination<Unit> {
    val navController = navController()
    SimpleScreen("Savable data: Root") {
        var counter by rememberSaveable { mutableIntStateOf(0) }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircleButton("-") { counter-- }
                Text(text = "Value: $counter", style = MaterialTheme.typography.bodyMedium)
                CircleButton("+") { counter++ }
            }
            Button({
                navController.navigate(
                    SavableDataExampleArgsScreen,
                    ParcelableNavArgs(counter)
                )
            }) {
                Text("Pass data to next screen")
            }
        }
    }
}

val SavableDataExampleArgsScreen by navDestination<ParcelableNavArgs> {
    val navController = navController()
    val args = navArgs()
    SimpleScreen("Savable data: Args") {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Received data: ${args.count}",
                style = MaterialTheme.typography.bodyMedium
            )
            BackButton(onClick = { navController.back() })
        }
    }
}
