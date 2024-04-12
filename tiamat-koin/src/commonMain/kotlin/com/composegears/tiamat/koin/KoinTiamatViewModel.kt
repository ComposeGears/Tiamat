package com.composegears.tiamat.koin

import androidx.compose.runtime.Composable
import com.composegears.tiamat.NavDestinationScope
import com.composegears.tiamat.TiamatViewModel
import com.composegears.tiamat.rememberViewModel
import org.koin.compose.currentKoinScope
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.scope.Scope

/**
 * Resolve TiamatViewModel from Koin
 *
 * @param parameters injected parameters into ViewModel
 */
@Composable
inline fun <reified Model : TiamatViewModel> NavDestinationScope<*>.koinTiamatViewModel(
    scope: Scope = currentKoinScope(),
    noinline parameters: ParametersDefinition? = null,
): Model = rememberViewModel { scope.get(parameters = parameters) }

/**
 * Resolve TiamatViewModel from Koin
 *
 * @param key provides unique key to create ViewModel
 * @param parameters injected parameters into ViewModel
 */
@Composable
inline fun <reified Model : TiamatViewModel> NavDestinationScope<*>.koinTiamatViewModel(
    key: String,
    scope: Scope = currentKoinScope(),
    noinline parameters: ParametersDefinition? = null,
): Model = rememberViewModel(key = key) { scope.get(parameters = parameters) }