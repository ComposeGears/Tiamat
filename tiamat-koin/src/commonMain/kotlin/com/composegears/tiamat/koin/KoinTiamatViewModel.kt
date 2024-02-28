package com.composegears.tiamat.koin

import androidx.compose.runtime.Composable
import com.composegears.tiamat.NavDestinationScope
import com.composegears.tiamat.TiamatViewModel
import com.composegears.tiamat.rememberViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.ParametersDefinition

@Composable
inline fun <reified Model : TiamatViewModel> NavDestinationScope<*>.koinTiamatViewModel(
    key: String? = null,
    noinline parameters: ParametersDefinition? = null,
): Model {
    val viewModel = koinInject<Model>(parameters = parameters)
    return rememberViewModel(key = key) { viewModel }
}
