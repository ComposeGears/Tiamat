package composegears.tiamat.sample.koin.viewmodel

import com.composegears.tiamat.TiamatViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal class SharedViewModel : TiamatViewModel() {
    private val _timer = MutableStateFlow(0)
    val timer = _timer.asStateFlow()

    init {
        viewModelScope.launch {
            while (isActive) {
                _timer.value++
                delay(1000)
            }
        }
    }
}