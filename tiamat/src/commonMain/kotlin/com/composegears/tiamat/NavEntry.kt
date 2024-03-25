package com.composegears.tiamat

import androidx.compose.runtime.Stable
import androidx.compose.runtime.saveable.SaveableStateRegistry

/**
 * Internal class
 *
 * Hold nav entry information and allow to save/restore state base on storage mode
 */
@Stable
internal class NavEntry<Args> private constructor(
    val uuid: Long,
    val destination: NavDestination<Args>,
    val parentDataStorage: DataStorage,
    var navArgs: Args? = null,
    var freeArgs: Any? = null,
) {
    companion object {

        private var nextUUID = 0L

        private const val KEY_UUID = "uuid"
        private const val KEY_NAME = "name"
        private const val KEY_NAV_ARGS = "navArgs"
        private const val KEY_FREE_ARGS = "freeArgs"
        private const val KEY_NAV_RESULT = "navResult"
        private const val KEY_SAVED_STATE = "savedState"

        internal fun restoreNavEntry(
            savedState: Map<String, Any?>,
            parentDataStorage: DataStorage,
            destinations: Array<NavDestination<*>>,
        ): NavEntry<*> {
            val destination = (savedState[KEY_NAME] as String).let { name -> destinations.first { it.name == name } }
            return restoreNavEntry(destination, savedState, parentDataStorage)
        }

        @Suppress("UNCHECKED_CAST")
        private fun <Args> restoreNavEntry(
            destination: NavDestination<Args>,
            savedState: Map<String, Any?>,
            parentDataStorage: DataStorage,
        ) = NavEntry(
            uuid = savedState[KEY_UUID] as Long,
            destination = destination,
            parentDataStorage = parentDataStorage,
            navArgs = savedState[KEY_NAV_ARGS] as Args,
            freeArgs = savedState[KEY_FREE_ARGS],
        ).also {
            it.savedState = savedState[KEY_SAVED_STATE] as? Map<String, List<Any?>>?
            it.navResult = savedState[KEY_NAV_RESULT]
            // ensure next uuid will be unique after restoring state of this one
            nextUUID = maxOf(nextUUID, it.uuid + 1)
        }
    }

    private val storageKey = "NavEntry#${destination.name}#$uuid"
    val entryStorage: DataStorage = parentDataStorage.data.getOrPut(storageKey, ::DataStorage) as DataStorage
    var navResult: Any? = null
    var savedState: Map<String, List<Any?>>? = null
    var savedStateRegistry: SaveableStateRegistry? = null

    constructor(
        destination: NavDestination<Args>,
        parentDataStorage: DataStorage,
        navArgs: Args? = null,
        freeArgs: Any? = null,
    ) : this(
        uuid = nextUUID++,
        destination = destination,
        parentDataStorage = parentDataStorage,
        navArgs = navArgs,
        freeArgs = freeArgs
    )

    internal fun saveState() {
        savedState = savedStateRegistry?.performSave()
    }

    internal fun toShortSavedState(): Map<String, Any?> = mapOf(
        KEY_UUID to uuid,
        KEY_NAME to destination.name,
        KEY_SAVED_STATE to savedState,
    )

    internal fun toFullSavedState(): Map<String, Any?> = mapOf(
        KEY_UUID to uuid,
        KEY_NAME to destination.name,
        KEY_SAVED_STATE to savedState,
        KEY_NAV_ARGS to navArgs,
        KEY_FREE_ARGS to freeArgs,
        KEY_NAV_RESULT to navResult,
    )

    fun close() {
        entryStorage.close()
        parentDataStorage.data.remove(storageKey)
    }
}