package com.composegears.tiamat

import androidx.compose.runtime.saveable.SaveableStateRegistry

/**
 * Internal class
 *
 * Hold nav entry information and allow to save/restore state base on storage mode
 */
internal class NavEntry(
    val destination: NavDestination<*>,
    var navArgs: Any? = null,
    var navResult: Any? = null,
    var entryStorage: DataStorage? = null,
    var savedState: Map<String, List<Any?>>? = null,
    var savedStateRegistry: SaveableStateRegistry? = null,
) {
    companion object {

        private var nextUUID = 0L

        private const val KEY_UUID = "uuid"
        private const val KEY_NAME = "name"
        private const val KEY_NAV_ARGS = "navArgs"
        private const val KEY_NAV_RESULT = "navResult"
        private const val KEY_SAVED_STATE = "savedState"

        @Suppress("UNCHECKED_CAST")
        internal fun Map<String, Any?>.restoreNavEntry(
            storageMode: StorageMode,
            destinations: Array<NavDestination<*>>,
        ) = NavEntry(
            destination = (this[KEY_NAME] as String).let { name -> destinations.first { it.name == name } },
            savedState = this[KEY_SAVED_STATE] as? Map<String, List<Any?>>?,
        ).also {
            it.uuid = this[KEY_UUID] as Long
            nextUUID = maxOf(nextUUID, it.uuid + 1)
            if (storageMode == StorageMode.Savable) {
                it.navArgs = this[KEY_NAV_ARGS]
                it.navResult = this[KEY_NAV_RESULT]
            }
        }
    }

    internal var uuid: Long = nextUUID++
        private set

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
            state[KEY_NAV_RESULT] = navResult
        }
        return state
    }
}