package com.composegears.tiamat.navigation

public typealias SavedState = Map<String, Any?>

public fun SavedState(vararg pairs: Pair<String, Any?>): SavedState = mapOf(*pairs)