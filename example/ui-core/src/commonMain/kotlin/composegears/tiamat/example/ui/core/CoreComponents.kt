package composegears.tiamat.example.ui.core

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.NavDestinationScope
import com.composegears.tiamat.navController

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


@Composable
fun AppButton(
    text: String,
    enabled: Boolean = true,
    startIcon: ImageVector? = null,
    endIcon: ImageVector? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        AppButtonContent(text, startIcon, endIcon)
    }
}

@Composable
fun AppOutlinedButton(
    text: String,
    enabled: Boolean = true,
    startIcon: ImageVector? = null,
    endIcon: ImageVector? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        AppButtonContent(text, startIcon, endIcon)
    }
}

@Composable
fun AppTextButton(
    text: String,
    enabled: Boolean = true,
    startIcon: ImageVector? = null,
    endIcon: ImageVector? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        AppButtonContent(text, startIcon, endIcon)
    }
}

@Composable
internal fun AppButtonContent(
    text: String,
    startIcon: ImageVector? = null,
    endIcon: ImageVector? = null,
) {
    Row {
        if (startIcon != null) Icon(startIcon, "", Modifier.size(24.dp))
        Text(
            text, Modifier.padding(
                start = if (startIcon == null) 12.dp else 0.dp,
                end = if (endIcon == null) 12.dp else 0.dp,
            )
        )
        if (endIcon != null) Icon(endIcon, "", Modifier.size(24.dp))
    }
}

@Composable
fun <T> NavDestinationScope<T>.Screen(
    title: String,
    backButton: Boolean = true,
    body: @Composable NavDestinationScope<T>.() -> Unit
) {
    val nc = navController()
    Column(Modifier.fillMaxSize().navigationBarsPadding()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.heightIn(min = 48.dp).statusBarsPadding(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (backButton) IconButton(
                    onClick = { nc.back() }
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "")
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
        HorizontalDivider()
        body()
    }
}