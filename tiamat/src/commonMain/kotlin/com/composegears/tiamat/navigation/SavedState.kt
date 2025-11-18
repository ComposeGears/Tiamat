package com.composegears.tiamat.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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

/**
 * Represents a mutable saved state as a map of string keys to any values.
 * Used to store and restore the state of navigation components when modifications are needed.
 */
public typealias MutableSavedState = MutableMap<String, Any?>

/**
 * Creates a MutableSavedState from the given key-value pairs.
 *
 * @param pairs Key-value pairs to include in the SavedState
 * @return A MutableSavedState containing the given pairs
 */
public fun MutableSavedState(vararg pairs: Pair<String, Any?>): MutableSavedState = mutableMapOf(*pairs)

/**
 * A record that manages a specific value within a saved state
 */
@Suppress("UNCHECKED_CAST", "UseDataClass")
public class SavedStateRecord<T> internal constructor(
    private val savedState: MutableSavedState,
    private val key: String,
    private val initialValueProvider: () -> T,
) {
    internal val flow = MutableStateFlow(savedState.getOrPut(key, initialValueProvider) as T)

    public var value: T
        get() = savedState.getOrPut(key, initialValueProvider) as T
        set(value) {
            savedState[key] = value
            flow.value = value
        }
}

/**
 * Creates a SavedStateRecord for the given key and initial value within this mutable saved state.
 *
 * @param key The key to associate with the record
 * @param initialValueProvider The initial value provider to use if the key doesn't exist in the saved state
 * @return A SavedStateRecord that manages the value for the given key
 */
public fun <T> MutableSavedState.recordOf(key: String, initialValueProvider: () -> T): SavedStateRecord<T> =
    SavedStateRecord(this, key, initialValueProvider)

/**
 * Creates a SavedStateRecord for the given key and initial value within this mutable saved state.
 *
 * @param key The key to associate with the record
 * @param initialValue The initial value to use if the key doesn't exist in the saved state
 * @return A SavedStateRecord that manages the value for the given key
 */
public fun <T> MutableSavedState.recordOf(key: String, initialValue: T): SavedStateRecord<T> =
    SavedStateRecord(this, key) { initialValue }

/**
 * Returns a read-only StateFlow that emits the current value of this SavedStateRecord
 * and all subsequent changes to it.
 *
 * @return A StateFlow that observes changes to this record's value
 */
public fun <T> SavedStateRecord<T>.asStateFlow(): StateFlow<T> = flow.asStateFlow()
