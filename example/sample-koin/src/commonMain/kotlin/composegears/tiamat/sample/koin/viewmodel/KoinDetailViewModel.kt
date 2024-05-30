package composegears.tiamat.sample.koin.viewmodel

import com.composegears.tiamat.TiamatViewModel
import composegears.tiamat.sample.koin.viewmodel.KoinDetailViewModel.Companion.KoinDetailState.Loading
import composegears.tiamat.sample.koin.viewmodel.KoinDetailViewModel.Companion.KoinDetailState.Success
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class KoinDetailViewModel(private val params: String) : TiamatViewModel() {

    companion object {
        internal sealed interface KoinDetailState {
            data object Loading : KoinDetailState
            data class Success(val result: String) : KoinDetailState
        }
    }

    private val _state = MutableStateFlow<KoinDetailState>(Loading)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            delay(500)

            val result = "$params: ${hashCode()}"
            _state.update { Success(result) }
        }
    }
}