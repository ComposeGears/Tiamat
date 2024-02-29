package com.composegears.tiamat

/**
 * We can not call T::class in @Composable functions,
 *
 * workaround is to call it outside of @Composable via regular inline fun
 */
inline fun <reified T : Any> className(): String = T::class.qualifiedName!!