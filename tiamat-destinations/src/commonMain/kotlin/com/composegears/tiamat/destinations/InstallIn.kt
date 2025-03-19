package com.composegears.tiamat.destinations

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
public annotation class InstallIn(val obj: KClass<out TiamatDestinations>)