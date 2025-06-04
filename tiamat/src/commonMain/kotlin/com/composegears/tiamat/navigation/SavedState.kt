package com.composegears.tiamat.navigation

/**
 * Represents a saved state as a map of string keys to any values.
 * Used to store and restore the state of navigation components.
 */
public typealias SavedState = Map<String, Any?>

/**
 * Creates a SavedState from the given key-value pairs.
 *
 * @param pairs Key-value pairs to include in the SavedState
 * @return A SavedState containing the given pairs
 */
public fun SavedState(vararg pairs: Pair<String, Any?>): SavedState = mapOf(*pairs)