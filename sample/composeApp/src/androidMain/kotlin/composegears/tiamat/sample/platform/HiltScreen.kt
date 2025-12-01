package composegears.tiamat.sample.platform

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.composegears.tiamat.compose.navDestination
import composegears.tiamat.sample.ui.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

class HiltDependency @Inject constructor() {
    fun getMessage() = "Hello from Hilt VM Dependency!"
}

@HiltViewModel
class HiltSampleViewModel @Inject constructor(
    private val hiltDependency: HiltDependency
) : ViewModel() {
    fun getMessage() = hiltDependency.getMessage()
}

val HiltSample by navDestination {
    Screen(title = "Hilt") {
        val viewModel: HiltSampleViewModel = hiltViewModel()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = viewModel.getMessage(),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

