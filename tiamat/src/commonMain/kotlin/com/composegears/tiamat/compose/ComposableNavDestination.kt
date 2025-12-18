package com.composegears.tiamat.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.composegears.tiamat.navigation.NavController
import com.composegears.tiamat.navigation.NavEntry

// ------------- NavDestinationScope extras : general ------------------------------------------------------------------

/**
 * @return The current [NavController].
 */
@Composable
@Suppress("UnusedReceiverParameter")
public fun NavDestinationScope<*>.navController(): NavController =
    LocalNavController.current ?: error("not attached to navController")

/**
 * @return The current [NavEntry].
 */
@Composable
public fun NavDestinationScope<*>.navEntry(): NavEntry<*> = navEntry

/**
 * Gets the extension of the specified type from the current [ComposeNavDestination].
 *
 * @param P The type of the extension.
 * @return The extension of the specified type, or null if not found.
 */
@Composable
public inline fun <reified P : NavExtension<*>> NavDestinationScope<*>.ext(): P? =
    navEntry().destination.ext<P>()

// ------------- NavDestinationScope extras : navArgs ------------------------------------------------------------------

/**
 * Gets the navigation arguments from the current [NavEntry] or throw an exception.
 *
 * @param Args The type of the navigation arguments.
 * @return The navigation arguments.
 */
@Composable
public fun <Args : Any> NavDestinationScope<Args>.navArgs(): Args =
    navArgsOrNull() ?: error("args not provided or null, consider use navArgsOrNull()")

/**
 * Gets the navigation arguments from the current [NavEntry], or null if not provided.
 *
 * @param Args The type of the navigation arguments.
 * @return The navigation arguments, or null if not provided.
 */
@Suppress("CastToNullableType")
@Composable
public fun <Args : Any> NavDestinationScope<Args>.navArgsOrNull(): Args? = remember {
    navEntry.getNavArgs()
}

/**
 * Clears the navigation arguments from the [NavEntry].
 */
public fun NavDestinationScope<*>.clearNavArgs() {
    navEntry.clearNavArgs()
}

// ------------- NavDestinationScope extras : freeArgs -----------------------------------------------------------------

/**
 * Gets the free arguments from the current [NavEntry].
 *
 * @param T The type of the free arguments to retrieve.
 * @return The free arguments of the specified type,or null if not present or not of type [T].
 */
@Composable
public inline fun <reified T> NavDestinationScope<*>.freeArgs(): T? = remember {
    navEntry.getFreeArgs()
}

/**
 * Clears the free arguments from the [NavEntry].
 */
public fun NavDestinationScope<*>.clearFreeArgs() {
    navEntry.clearFreeArgs()
}

// ------------- NavDestinationScope extras : navResult ----------------------------------------------------------------

/**
 * Gets the navigation result from the current [NavEntry].
 *
 * @param T The type of the navigation result to retrieve.
 * @return The navigation result of the specified type, or null if not present or not of type [T].
 */
@Composable
public inline fun <reified T> NavDestinationScope<*>.navResult(): T? = remember {
    navEntry.getNavResult()
}

/**
 * Clears the navigation result from the [NavEntry].
 */
public fun NavDestinationScope<*>.clearNavResult() {
    navEntry.clearNavResult()
}