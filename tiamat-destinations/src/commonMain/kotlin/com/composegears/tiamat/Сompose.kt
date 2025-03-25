package com.composegears.tiamat

import androidx.compose.runtime.Composable
import com.composegears.tiamat.destinations.TiamatGraph

/**
 * Remembers a `NavController`.
 *
 * @param key The key for the NavController.
 * @param storageMode The storage mode for the NavController.
 * @param startDestination The start destination for the NavController.
 * @param graph The TiamatGraph that defines the navigation graph for the NavController.
 * @param configuration The action to be called after NavController created/restored.
 * @return The remembered NavController.
 */
@Composable
@Suppress("ComposableParamOrder")
public fun rememberNavController(
    key: String? = null,
    storageMode: StorageMode? = null,
    startDestination: NavDestination<*>? = null,
    graph: TiamatGraph,
    configuration: NavController.() -> Unit = {}
): NavController = rememberNavController(
    key = key,
    storageMode = storageMode,
    startDestination = startDestination,
    destinations = graph.destinations(),
    configuration = configuration,
)

/**
 * Remembers a `NavController`.
 *
 * @param key The key for the NavController.
 * @param storageMode The storage mode for the NavController.
 * @param startDestination The start destination for the NavController.
 * @param startDestinationNavArgs The navigation navArgs for the start destination.
 * @param startDestinationFreeArgs The navigation freeArgs for the start destination.
 * @param graph The TiamatGraph that defines the navigation graph for the NavController.
 * @param configuration The action to be called after NavController created/restored.
 * @return The remembered NavController.
 */
@Composable
@Suppress("ComposableParamOrder")
public fun <T> rememberNavController(
    key: String? = null,
    storageMode: StorageMode? = null,
    startDestination: NavDestination<T>,
    startDestinationNavArgs: T? = null,
    startDestinationFreeArgs: Any? = null,
    graph: TiamatGraph,
    configuration: NavController.() -> Unit = {}
): NavController = rememberNavController(
    key = key,
    storageMode = storageMode,
    startDestination = startDestination,
    startDestinationNavArgs = startDestinationNavArgs,
    startDestinationFreeArgs = startDestinationFreeArgs,
    destinations = graph.destinations(),
    configuration = configuration,
)

/**
 * Remembers a `NavController`.
 *
 * @param key The key for the NavController.
 * @param storageMode The storage mode for the NavController.
 * @param startDestination The start destination for the NavController.
 * @param graph The TiamatGraph that defines the navigation graph for the NavController.
 * @param configuration The action to be called after NavController created/restored.
 * @return The remembered NavController.
 */
@Composable
@Suppress("ComposableParamOrder")
public fun <T> rememberNavController(
    key: String? = null,
    storageMode: StorageMode? = null,
    startDestination: NavEntry<T>?,
    graph: TiamatGraph,
    configuration: NavController.() -> Unit = {}
): NavController = rememberNavController(
    key = key,
    storageMode = storageMode,
    startDestination = startDestination,
    destinations = graph.destinations(),
    configuration = configuration,
)