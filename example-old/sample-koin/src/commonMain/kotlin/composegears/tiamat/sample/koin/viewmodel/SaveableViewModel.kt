package composegears.tiamat.sample.koin.viewmodel

import com.composegears.tiamat.Saveable
import com.composegears.tiamat.SavedState
import com.composegears.tiamat.TiamatViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal class SaveableViewModel(savedState: SavedState?) : TiamatViewModel(), Saveable {
    private val _timer = MutableStateFlow(0)
    val timer = _timer.asStateFlow()

    init {
        _timer.value = savedState?.get("count") as? Int? ?: 0
        viewModelScope.launch {
            while (isActive) {
                _timer.value++
                delay(1000)
            }
        }
    }

    override fun saveToSaveState() = mapOf("count" to timer.value)
}