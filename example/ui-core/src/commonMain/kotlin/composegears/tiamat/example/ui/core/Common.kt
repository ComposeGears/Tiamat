package composegears.tiamat.example.ui.core

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ViewModelInfoCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp), RoundedCornerShape(10.dp))
            .padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}

@Composable
fun ColumnScope.ViewModelInfoBody(name: String, hashCode: Int, timer: Int) {
    TextBodyLarge(name)
    TextCaption("hashCode: $hashCode")
    TextCaption("timer: $timer")
}

@Composable
fun ViewModelInfo(name: String, hashCode: Int, timer: Int) {
    ViewModelInfoCard {
        ViewModelInfoBody(
            name = name,
            hashCode = hashCode,
            timer = timer
        )
    }
}

@Composable
fun NextButton(text: String = "Next", onClick: () -> Unit) {
    Button(
        onClick = onClick,
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 8.dp,
            top = 8.dp,
            bottom = 8.dp
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text)
            Icon(Icons.AutoMirrored.Filled.NavigateNext, "")
        }
    }
}

@Composable
fun BackButton(text: String = "Back", onClick: () -> Unit) {
    Button(
        onClick = onClick,
        contentPadding = PaddingValues(
            start = 8.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = 8.dp
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.NavigateBefore, "")
            Text(text)
        }
    }
}

@Composable
fun ExitButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        contentPadding = PaddingValues(
            start = 12.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = 8.dp
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                imageVector = Icons.Default.Close,
                contentDescription = ""
            )
            Text(text)
        }
    }
}

@Composable
fun TextButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text)
    }
}

@Composable
fun CircleButton(text: String, onClick: () -> Unit) {
    Button(
        modifier = Modifier.size(40.dp),
        onClick = onClick,
        contentPadding = PaddingValues()
    ) {
        Text(text)
    }
}

@Composable
fun Spacer(height: Dp = 16.dp) {
    Spacer(modifier = Modifier.height(height))
}

@Composable
fun TextCaption(text: String) {
    Text(
        text = text,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.labelSmall
    )
}

@Composable
fun TextBody(text: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = text,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
fun TextBodyLarge(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge
    )
}