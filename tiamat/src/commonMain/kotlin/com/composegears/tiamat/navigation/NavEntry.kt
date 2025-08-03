package com.composegears.tiamat.navigation

import androidx.compose.runtime.Stable
import androidx.lifecycle.*
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import com.composegears.tiamat.ExcludeFromTests
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import kotlinx.serialization.serializerOrNull
import kotlin.reflect.KClass
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import androidx.savedstate.SavedState as SavedStateX

/**
 * Represents a navigation entry in the navigation stack.
 *
 * A NavEntry is a combination of a destination and its associated navigation arguments.
 * It can also store results, free arguments, and maintain view models.
 *
 * @param destination The navigation destination this entry represents
 * @param navArgs Optional typed arguments to pass to the destination
 * @param freeArgs Optional untyped arguments to pass to the destination
 * @param navResult Optional result value for this entry
 */
@Stable
public class NavEntry<Args : Any> public constructor(
    destination: NavDestination<Args>,
    private var navArgs: Args? = null,
    private var freeArgs: Any? = null,
    private var navResult: Any? = null
) : RouteElement, ViewModelStoreOwner, LifecycleOwner {

    public companion object {

        private const val KEY_DESTINATION = "destination"
        private const val KEY_UUID = "uuid"
        private const val KEY_NAV_ARGS = "navArgs"
        private const val KEY_FREE_ARGS = "freeArgs"
        private const val KEY_NAV_RESULT = "navResult"
        private const val KEY_SAVED_STATE = "savedState"
        private const val KEY_NAV_CONTROLLERS = "navControllers"

        @Suppress("UNCHECKED_CAST")
        internal fun restoreFromSavedState(
            parent: NavController?,
            savedState: SavedState
        ): NavEntry<*> {
            val destination = savedState[KEY_DESTINATION] as? String
                ?: error("Unable to restore NavEntry: destination is null")
            val uuid = savedState[KEY_UUID] as? String
                ?: error("Unable to restore NavEntry: uuid is null")
            val navArgs = savedState[KEY_NAV_ARGS]
            val freeArgs = savedState[KEY_FREE_ARGS]
            val navResult = savedState[KEY_NAV_RESULT]
            val entrySavedState = savedState[KEY_SAVED_STATE] as? SavedState
            val navControllers = savedState[KEY_NAV_CONTROLLERS] as? SavedState
            return NavEntry(
                destination = UnresolvedDestination(destination),
                navArgs = navArgs,
                freeArgs = freeArgs,
                navResult = navResult,
            ).also {
                it.uuid = uuid
                it.savedState = entrySavedState
                it.navControllerStore.loadFromSavedState(parent, navControllers)
            }
        }
    }

    // used to get current SavedState when attached to UI
    private var savedStateSaver: (() -> SavedState)? = null
    internal var savedState: SavedState? = null

    internal var isAttachedToNavController = false
        private set
    internal var isAttachedToUI = false
        private set
    internal var isResolved = destination !is UnresolvedDestination
        private set

    @OptIn(ExperimentalUuidApi::class)
    internal var uuid: String = Uuid.random().toHexString()
        private set

    /**
     * Returns the [NavControllerStore] associated with this NavEntry.
     */
    public val navControllerStore: NavControllerStore = NavControllerStore()

    /**
     * Returns the [ViewModelStore] associated with this NavEntry.
     */
    public override val viewModelStore: ViewModelStore = ViewModelStore()

    private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)

    /**
     * Returns the [Lifecycle] associated with this NavEntry.
     */
    public override val lifecycle: Lifecycle = lifecycleRegistry

    /**
     * The destination this entry represents.
     */
    public var destination: NavDestination<Args> = destination
        private set

    init {
        lifecycleRegistry.currentState = Lifecycle.State.INITIALIZED
    }

    /**
     * Returns the typed navigation arguments for this entry, or null if not present.
     */
    public fun getNavArgs(): Args? = tryDecode(navArgs, destination.argsClazz) { navArgs = it }

    /**
     * Clears the navigation arguments for this entry.
     */
    public fun clearNavArgs() {
        navArgs = null
    }

    /**
     * Returns the free (untyped) arguments for this entry as type [T], or null if not present or not of type [T].
     */
    public inline fun <reified T : Any> getFreeArgs(): T? = getFreeArgs(T::class)

    /**
     * Clears the free (untyped) arguments for this entry.
     */
    public fun clearFreeArgs() {
        freeArgs = null
    }

    /**
     * Returns the navigation result for this entry as type [T], or null if not present or not of type [T].
     */
    public inline fun <reified T : Any> getNavResult(): T? = getNavResult(T::class)

    /**
     * Clears the navigation result for this entry.
     */
    public fun clearNavResult() {
        navResult = null
    }

    internal fun setNavResult(result: Any?) {
        navResult = result
    }

    @PublishedApi
    internal fun <T : Any> getFreeArgs(clazz: KClass<T>): T? = tryDecode(freeArgs, clazz) { freeArgs = it }

    @PublishedApi
    internal fun <T : Any> getNavResult(clazz: KClass<T>): T? = tryDecode(navResult, clazz) { navResult = it }

    public fun contentKey(): String = "${destination.name}-$uuid"

    @Suppress("UNCHECKED_CAST")
    internal fun resolveDestination(destinationResolver: (name: String) -> NavDestination<*>?) {
        destination = destinationResolver(destination.name)
            ?.let { it as? NavDestination<Args> }
            ?: error("Unable to resolve destination: ${destination.name}")
        isResolved = true
    }

    internal fun saveToSavedState(): SavedState {
        if (savedStateSaver != null) savedState = savedStateSaver!!()
        return SavedState(
            KEY_DESTINATION to destination.name,
            KEY_UUID to uuid,
            KEY_NAV_ARGS to tryEncode(navArgs),
            KEY_FREE_ARGS to tryEncode(freeArgs),
            KEY_NAV_RESULT to tryEncode(navResult),
            KEY_SAVED_STATE to savedState,
            KEY_NAV_CONTROLLERS to navControllerStore.saveToSavedState(),
        )
    }

    internal fun setSavedStateSaver(saver: (() -> SavedState)?) {
        savedStateSaver = saver
    }

    internal fun attachToNavController() {
        isAttachedToNavController = true
    }

    internal fun detachFromNavController() {
        isAttachedToNavController = false
        if (!isAttachedToUI) close()
    }

    internal fun ensureDetachedAndAttach() {
        if (isAttachedToNavController) error("NavEntry is already attached to a NavController")
        attachToNavController()
    }

    internal fun attachToUI() {
        isAttachedToUI = true
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    internal fun detachFromUI() {
        isAttachedToUI = false
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        if (!isAttachedToNavController) close()
    }

    private fun close() {
        viewModelStore.clear()
        navControllerStore.clear()
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    @OptIn(InternalSerializationApi::class)
    private fun tryEncode(src: Any?): Any? = src
        // no need to re-save saved state or Unit's
        ?.takeIf { it !is SavedStateX }
        ?.let { it::class.serializerOrNull() }
        ?.let { encodeToSavedState(serializer = it as KSerializer<Any>, value = src) }
        ?: src

    @Suppress("UNCHECKED_CAST")
    @OptIn(InternalSerializationApi::class)
    private fun <T : Any> tryDecode(
        src: Any?,
        clazz: KClass<T>,
        onDecoded: (T) -> Unit = {}
    ): T? = when {
        src == null -> null
        src::class == clazz || clazz == Any::class -> src as T
        src is SavedStateX -> {
            runCatching {
                decodeFromSavedState(clazz.serializer(), src)
            }.getOrNull()?.also(onDecoded)
        }
        else -> null
    }

    @ExcludeFromTests
    override fun toString(): String =
        "NavEntry(destination=${destination.name})"
}