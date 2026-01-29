package composegears.tiamat.sample.content.state.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import composegears.tiamat.sample.icons.Icons
import composegears.tiamat.sample.icons.KeyboardArrowLeft
import composegears.tiamat.sample.icons.KeyboardArrowRight
import composegears.tiamat.sample.ui.AppButton
import composegears.tiamat.sample.ui.VSpacer

@Composable
fun ViewModelScreen1Content(
    sharedViewModelCounter: Int,
    onNext: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.widthIn(max = 400.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
        ) {
            Text(
                text = "Screen 1",
                style = MaterialTheme.typography.headlineMedium,
            )
            CounterCell(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                text = "Shared ViewModel",
                description = "Shared across screens",
                counter = sharedViewModelCounter
            )
            AppButton(
                text = "Next",
                endIcon = Icons.KeyboardArrowRight,
                onClick = onNext,
            )
        }
    }
}

@Composable
fun ViewModelScreen2Content(
    viewModelCounter: Int,
    sharedViewModelCounter: Int,
    saveableViewModelCounter: Int,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.widthIn(max = 400.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Screen 2",
                style = MaterialTheme.typography.headlineMedium,
            )
            CounterCell(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                text = "ViewModel",
                description = "Scoped to destination",
                counter = viewModelCounter
            )
            CounterCell(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                text = "Shared ViewModel",
                description = "Shared across screens",
                counter = sharedViewModelCounter
            )
            CounterCell(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                text = "Saveable ViewModel",
                description = "Survives process death",
                counter = saveableViewModelCounter
            )
            VSpacer(12.dp)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💡 Try this:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    VSpacer(12.dp)
                    InstructionItem("1. Go next screen, then back → ViewModels are restored")
                    VSpacer(8.dp)
                    InstructionItem("2. Go back & reopen → ViewModels recreated (except shared)")
                    VSpacer(8.dp)
                    InstructionItem("3. Android: Hide & reopen app → Saveable ViewModel restores state")
                }
            }

            VSpacer(12.dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppButton(
                    text = "Back",
                    startIcon = Icons.KeyboardArrowLeft,
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                )
                AppButton(
                    text = "Next",
                    endIcon = Icons.KeyboardArrowRight,
                    onClick = onNext,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun ViewModelScreen3Content(
    sharedViewModelCounter: Int,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.widthIn(max = 400.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Screen 3",
                style = MaterialTheme.typography.headlineMedium,
            )
            CounterCell(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                text = "Shared ViewModel",
                description = "Shared across screens",
                counter = sharedViewModelCounter
            )
            AppButton(
                text = "Back",
                startIcon = Icons.KeyboardArrowLeft,
                onClick = onBack,
            )
        }
    }
}

@Composable
private fun CounterCell(
    containerColor: Color,
    textColor: Color = contentColorFor(containerColor),
    text: String,
    description: String,
    counter: Int,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    color = textColor.copy(alpha = 0.7f)
                )
                VSpacer(4.dp)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.6f)
                )
            }
            Text(
                text = "$counter",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
private fun InstructionItem(text: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier.padding(end = 8.dp),
            text = "•",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            modifier = Modifier.weight(1f),
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
