package com.composegears.tiamat.compose

import androidx.compose.animation.ContentTransform
import com.composegears.tiamat.navigation.EntryData
import com.composegears.tiamat.navigation.NavController
import com.composegears.tiamat.navigation.NavDestination
import com.composegears.tiamat.navigation.NavDestination.Companion.toNavEntry
import com.composegears.tiamat.navigation.NavEntry

// ------------ Navigate -----------------------------------------------------------------------------------------------

/**
 * Navigates to a new destination.
 * The current destination is added to the back stack.
 *
 * @param entry The navigation entry to navigate to
 * @param navArgs Optional typed arguments to pass to the destination
 * @param freeArgs Optional untyped arguments to pass to the destination
 * @param transition Optional content transform animation for the transition
 * @param transitionController Optional controller for managing the transition programmatically
 */
public fun <Args : Any> NavController.navigate(
    entry: NavDestination<Args>,
    navArgs: Args? = null,
    freeArgs: Any? = null,
    transition: ContentTransform? = null,
    transitionController: TransitionController? = null
): Unit = navigate(
    entry = entry.toNavEntry(
        navArgs = navArgs,
        freeArgs = freeArgs,
        navResult = null
    ),
    transition = transition,
    transitionController = transitionController
)

/**
 * Navigates to a new destination.
 * The current destination is added to the back stack.
 *
 * @param entry The navigation entry to navigate to
 * @param navArgs Optional typed arguments to pass to the destination
 * @param freeArgs Optional untyped arguments to pass to the destination
 * @param transition Optional content transform animation for the transition
 * @param transitionController Optional controller for managing the transition programmatically
 */
public fun <Args : Any> NavController.navigate(
    entry: NavDestination<Args>,
    navArgs: EntryData<Args>,
    freeArgs: Any? = null,
    transition: ContentTransform? = null,
    transitionController: TransitionController? = null
): Unit = navigate(
    entry = entry.toNavEntry(
        navArgs = navArgs,
        freeArgs = freeArgs,
        navResult = null
    ),
    transition = transition,
    transitionController = transitionController
)

/**
 * Navigates to a new destination.
 * The current destination is added to the back stack.
 *
 * @param entry The navigation entry to navigate to
 * @param transition Optional content transform animation for the transition
 * @param transitionController Optional controller for managing the transition programmatically
 */
public fun <Args : Any> NavController.navigate(
    entry: NavEntry<Args>,
    transition: ContentTransform? = null,
    transitionController: TransitionController? = null
): Unit = navigate(
    entry = entry,
    transitionData = TransitionData(
        contentTransform = transition,
        transitionController = transitionController
    )
)

// ------------ Replace ------------------------------------------------------------------------------------------------

/**
 * Replaces the current destination with a new one.
 * The current destination is not added to the back stack.
 *
 * @param entry The navigation entry to replace with
 * @param navArgs Optional typed arguments to pass to the destination
 * @param freeArgs Optional untyped arguments to pass to the destination
 * @param transition Optional content transform animation for the transition
 * @param transitionController Optional controller for managing the transition programmatically
 */
public fun <Args : Any> NavController.replace(
    entry: NavDestination<Args>,
    navArgs: Args? = null,
    freeArgs: Any? = null,
    transition: ContentTransform? = null,
    transitionController: TransitionController? = null
): Unit = replace(
    entry = entry.toNavEntry(
        navArgs = navArgs,
        freeArgs = freeArgs,
        navResult = null
    ),
    transition = transition,
    transitionController = transitionController
)

/**
 * Replaces the current destination with a new one.
 * The current destination is not added to the back stack.
 *
 * @param entry The navigation entry to replace with
 * @param navArgs Optional typed arguments to pass to the destination
 * @param freeArgs Optional untyped arguments to pass to the destination
 * @param transition Optional content transform animation for the transition
 * @param transitionController Optional controller for managing the transition programmatically
 */
public fun <Args : Any> NavController.replace(
    entry: NavDestination<Args>,
    navArgs: EntryData<Args>,
    freeArgs: Any? = null,
    transition: ContentTransform? = null,
    transitionController: TransitionController? = null
): Unit = replace(
    entry = entry.toNavEntry(
        navArgs = navArgs,
        freeArgs = freeArgs,
        navResult = null
    ),
    transition = transition,
    transitionController = transitionController
)

/**
 * Replaces the current destination with a new one.
 * The current destination is not added to the back stack.
 *
 * @param entry The navigation entry to replace with
 * @param transition Optional content transform animation for the transition
 * @param transitionController Optional controller for managing the transition programmatically
 */
public fun <Args : Any> NavController.replace(
    entry: NavEntry<Args>,
    transition: ContentTransform? = null,
    transitionController: TransitionController? = null
): Unit = replace(
    entry = entry,
    transitionData = TransitionData(
        contentTransform = transition,
        transitionController = transitionController
    )
)

// ------------ popToTop -----------------------------------------------------------------------------------------------

/**
 * Pops the back stack to an existing destination, or navigates to it if not in the back stack.
 *
 * @param dest The destination to pop to
 * @param transition Optional content transform animation for the transition
 * @param transitionController Optional controller for managing the transition programmatically
 * @param orElse Action to perform if the destination is not found in the back stack
 */
public fun <Args : Any> NavController.popToTop(
    dest: NavDestination<Args>,
    transition: ContentTransform? = null,
    transitionController: TransitionController? = null,
    orElse: NavController.() -> Unit = {
        navigate(
            entry = dest.toNavEntry(),
            transition = transition,
            transitionController = transitionController
        )
    }
): Unit = popToTop(
    dest = dest,
    transitionData = TransitionData(
        contentTransform = transition,
        transitionController = transitionController
    ),
    orElse = orElse
)

// ------------ Back ---------------------------------------------------------------------------------------------------

/**
 * Navigates back in the navigation hierarchy.
 *
 * @param to Optional destination to navigate back to
 * @param result Optional result to pass to the destination
 * @param inclusive Whether to include the destination in the back operation
 * @param recursive Whether to recursively navigate back if current back operation impossible
 * @param transition Optional content transform animation for the transition
 * @param transitionController Optional controller for managing the transition programmatically
 * @return True if back navigation was handled, false otherwise
 */
public fun NavController.back(
    to: NavDestination<*>? = null,
    result: Any? = null,
    inclusive: Boolean = false,
    recursive: Boolean = true,
    transition: ContentTransform? = null,
    transitionController: TransitionController? = null,
): Boolean = back(
    to = to,
    result = result,
    inclusive = inclusive,
    recursive = recursive,
    transitionData = TransitionData(
        contentTransform = transition,
        transitionController = transitionController
    )
)