package com.composegears.tiamat.compose

import kotlin.reflect.KClass

public class GenericScopeAction internal constructor(
    private val source: (KClass<out Any>) -> Any?
) {
    private var result: Any? = null

    public inline fun <reified T : Any> type(
        noinline action: (T) -> Unit = {}
    ) {
        tryType(T::class) {
            action(it)
            it
        }
    }

    public inline fun <reified T : Any> mappedType(
        noinline mapper: (T) -> Any
    ): Unit = tryType(T::class, mapper)

    @PublishedApi
    @Suppress("UNCHECKED_CAST")
    internal fun <T : Any> tryType(clazz: KClass<T>, mapper: (T) -> Any = { it }) {
        if (result == null) source(clazz)?.apply {
            result = mapper(this as T)
        }
    }

    internal fun result(): Any? = result
}