package com.composegears.tiamat

import androidx.compose.runtime.saveable.SaveableStateRegistry

/**
 * Internal class
 *
 * Hold nav entry information and allow to save/restore state base on storage mode
 */
internal class NavEntry private constructor(
    val uuid: Long,
    val destination: NavDestination<*>,
    var navArgs: Any? = null,
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

        @Suppress("UNCHECKED_CAST")
        internal fun Map<String, Any?>.restoreNavEntry(
            storageMode: StorageMode,
            destinations: Array<NavDestination<*>>,
        ) = NavEntry(
            uuid = this[KEY_UUID] as Long,
            destination = (this[KEY_NAME] as String).let { name -> destinations.first { it.name == name } },
        ).also {
            // ensure next uuid will be unique after restoring state of this one
            nextUUID = maxOf(nextUUID, it.uuid + 1)
            it.savedState = this[KEY_SAVED_STATE] as? Map<String, List<Any?>>?
            if (storageMode == StorageMode.Savable) {
                it.navArgs = this[KEY_NAV_ARGS]
                it.freeArgs = this[KEY_FREE_ARGS]
                it.navResult = this[KEY_NAV_RESULT]
            }
        }
    }

    var navResult: Any? = null
    var entryStorage: DataStorage? = null
    var savedState: Map<String, List<Any?>>? = null
    var savedStateRegistry: SaveableStateRegistry? = null

    constructor(
        destination: NavDestination<*>,
        navArgs: Any? = null,
        freeArgs: Any? = null,
    ) : this(
        uuid = nextUUID++,
        destination = destination,
        navArgs = navArgs,
        freeArgs = freeArgs
    )

    internal fun saveState() {
        savedState = savedStateRegistry?.performSave()
    }

    internal fun toSavedState(
        storageMode: StorageMode
    ): Map<String, Any?> {
        val state = mutableMapOf(
            KEY_NAME to destination.name,
            KEY_SAVED_STATE to savedState,
            KEY_UUID to uuid
        )
        if (storageMode == StorageMode.Savable) {
            state[KEY_NAV_ARGS] = navArgs
            state[KEY_FREE_ARGS] = freeArgs
            state[KEY_NAV_RESULT] = navResult
        }
        return state
    }
}