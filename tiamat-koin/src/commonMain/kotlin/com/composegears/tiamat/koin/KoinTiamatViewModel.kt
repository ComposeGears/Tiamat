package com.composegears.tiamat.koin

import androidx.compose.runtime.Composable
import com.composegears.tiamat.*
import org.koin.compose.currentKoinScope
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.scope.Scope

/**
 * Resolve TiamatViewModel from Koin
 *
 * @param scope current Koin scope
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
 * @param scope current Koin scope
 * @param parameters injected parameters into ViewModel
 */
@Composable
inline fun <reified Model : TiamatViewModel> NavDestinationScope<*>.koinTiamatViewModel(
    key: String,
    scope: Scope = currentKoinScope(),
    noinline parameters: ParametersDefinition? = null,
): Model = rememberViewModel(key = key) { scope.get(parameters = parameters) }

/**
 * Resolve shared instance of TiamatViewModel from Koin to provided [NavController] (default is current)
 *
 * @param navController current navController to which the ViewModel will be attached
 * @param scope current Koin scope
 * @param parameters injected parameters into ViewModel
 */
@Composable
inline fun <reified Model : TiamatViewModel> NavDestinationScope<*>.koinSharedTiamatViewModel(
    navController: NavController = navController(),
    scope: Scope = currentKoinScope(),
    noinline parameters: ParametersDefinition? = null,
): Model = rememberSharedViewModel(navController = navController) { scope.get(parameters = parameters) }

/**
 * Resolve shared instance of TiamatViewModel from Koin to provided [NavController] (default is current)
 *
 * @param key provides unique key to create ViewModel
 * @param navController current navController to which the ViewModel will be attached
 * @param scope current Koin scope
 * @param parameters injected parameters into ViewModel
 */
@Composable
inline fun <reified Model : TiamatViewModel> NavDestinationScope<*>.koinSharedTiamatViewModel(
    key: String,
    navController: NavController = navController(),
    scope: Scope = currentKoinScope(),
    noinline parameters: ParametersDefinition? = null,
): Model = rememberSharedViewModel(key = key, navController = navController) { scope.get(parameters = parameters) }