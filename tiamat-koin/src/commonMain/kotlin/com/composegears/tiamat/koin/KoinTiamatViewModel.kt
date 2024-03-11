package com.composegears.tiamat.koin

import androidx.compose.runtime.Composable
import com.composegears.tiamat.NavDestinationScope
import com.composegears.tiamat.TiamatViewModel
import com.composegears.tiamat.rememberViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.ParametersDefinition

/**
 * Resolve TiamatViewModel from Koin
 *
 * @param parameters injected parameters into ViewModel
 */
@Composable
inline fun <reified Model : TiamatViewModel> NavDestinationScope<*>.koinTiamatViewModel(
    noinline parameters: ParametersDefinition? = null,
): Model {
    val viewModel = koinInject<Model>(parameters = parameters)
    return rememberViewModel { viewModel }
}

/**
 * Resolve TiamatViewModel from Koin
 *
 * @param key provides unique key to create ViewModel
 * @param parameters injected parameters into ViewModel
 */
@Composable
inline fun <reified Model : TiamatViewModel> NavDestinationScope<*>.koinTiamatViewModel(
    key: String,
    noinline parameters: ParametersDefinition? = null,
): Model {
    val viewModel = koinInject<Model>(parameters = parameters)
    return rememberViewModel(key = key) { viewModel }
}