package com.composegears.tiamat.destinations

import kotlin.reflect.KClass

/**
 * Annotation used to specify where a destination should be installed.
 * Use this with NavDestination definitions to automatically include them
 * in the appropriate navigation controllers.
 *
 * Example usage:
 * ```
 * // Define a key for a destination group
 * const val HomeNavDestinations = "HomeNavDestinations"
 *
 * // Annotate destinations to be included in that group
 * @InstallIn(HomeNavDestinations)
 * val HomeScreen by navDestination<Unit> { ... }
 *
 * // Use TiamatDestinations to get all destinations for a group
 * val destinations = TiamatDestinations.of(HomeNavDestinations) // todo
 * ```
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
public annotation class InstallIn(val target: String)