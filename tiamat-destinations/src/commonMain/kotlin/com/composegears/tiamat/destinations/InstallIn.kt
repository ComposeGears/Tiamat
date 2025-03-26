package com.composegears.tiamat.destinations

import com.composegears.tiamat.TiamatExperimentalApi
import kotlin.reflect.KClass

@Repeatable
@TiamatExperimentalApi
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
public annotation class InstallIn(val obj: KClass<out TiamatGraph>)