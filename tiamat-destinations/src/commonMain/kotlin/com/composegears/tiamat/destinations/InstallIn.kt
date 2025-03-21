package com.composegears.tiamat.destinations

import com.composegears.tiamat.TiamatExperimentalApi
import kotlin.reflect.KClass

@Repeatable
@TiamatExperimentalApi
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
public annotation class InstallIn(val obj: KClass<out TiamatGraph>)