package com.composegears.tiamat.destinations

import com.composegears.tiamat.TiamatExperimentalApi
import kotlin.reflect.KClass

/**
 * Annotation used to specify that a navigation destination should be included
 * in a particular [TiamatGraph].
 *
 * This annotation is processed at compile time to automatically register
 * navigation destinations with the specified graph.
 *
 * Multiple [InstallIn] annotations can be applied to the same element to include
 * a destination in multiple graphs.
 *
 * Example usage:
 * ```kotlin
 * @InstallIn(MainGraph::class)
 * val Home = navDestination<Unit> {
 *     // destination content
 * }
 * ```
 *
 * @property obj The [TiamatGraph] class where this destination should be installed
 */
@Repeatable
@TiamatExperimentalApi
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
public annotation class InstallIn(val obj: KClass<out TiamatGraph>)