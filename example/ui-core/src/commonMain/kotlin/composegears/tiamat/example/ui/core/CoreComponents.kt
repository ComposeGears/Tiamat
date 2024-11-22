package composegears.tiamat.example.ui.core

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun VSpacer(height: Dp = 16.dp) {
    Spacer(modifier = Modifier.height(height))
}

@Composable
fun HSpacer(width: Dp = 16.dp) {
    Spacer(modifier = Modifier.width(width))
}

@Composable
fun RowScope.FillSpace(weight: Float = 1f) {
    Spacer(modifier = Modifier.weight(weight))
}

@Composable
fun ColumnScope.FillSpace(weight: Float = 1f) {
    Spacer(modifier = Modifier.weight(weight))
}