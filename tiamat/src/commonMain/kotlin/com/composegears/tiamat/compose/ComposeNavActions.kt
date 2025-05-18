package com.composegears.tiamat.compose

import androidx.compose.animation.ContentTransform
import com.composegears.tiamat.navigation.NavController
import com.composegears.tiamat.navigation.NavDestination
import com.composegears.tiamat.navigation.NavDestination.Companion.toNavEntry
import com.composegears.tiamat.navigation.NavEntry

// ------------ Navigate -----------------------------------------------------------------------------------------------

public fun <Args> NavController.navigate(
    entry: NavDestination<Args>,
    navArgs: Args? = null,
    freeArgs: Any? = null,
    transition: ContentTransform? = null,
    transitionController: TransitionController? = null
) = navigate(
    entry = entry.toNavEntry(
        navArgs = navArgs,
        freeArgs = freeArgs,
        navResult = null
    ),
    transition = transition,
    transitionController = transitionController
)

public fun <Args> NavController.navigate(
    entry: NavEntry<Args>,
    transition: ContentTransform? = null,
    transitionController: TransitionController? = null
) = navigate(
    entry = entry,
    transitionData = TransitionData(
        contentTransform = transition,
        transitionController = transitionController
    )
)

// ------------ Replace ------------------------------------------------------------------------------------------------

public fun <Args> NavController.replace(
    entry: NavDestination<Args>,
    navArgs: Args? = null,
    freeArgs: Any? = null,
    transition: ContentTransform? = null,
    transitionController: TransitionController? = null
) = replace(
    entry = entry.toNavEntry(
        navArgs = navArgs,
        freeArgs = freeArgs,
        navResult = null
    ),
    transition = transition,
    transitionController = transitionController
)

public fun <Args> NavController.replace(
    entry: NavEntry<Args>,
    transition: ContentTransform? = null,
    transitionController: TransitionController? = null
) = replace(
    entry = entry,
    transitionData = TransitionData(
        contentTransform = transition,
        transitionController = transitionController
    )
)

// ------------ popToTop -----------------------------------------------------------------------------------------------

public fun <Args> NavController.popToTop(
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
) = popToTop(
    dest = dest,
    transitionData = TransitionData(
        contentTransform = transition,
        transitionController = transitionController
    ),
    orElse = orElse
)

// ------------ Back ---------------------------------------------------------------------------------------------------

public fun NavController.back(
    to: NavDestination<*>? = null,
    result: Any? = null,
    inclusive: Boolean = false,
    transition: ContentTransform? = null,
    transitionController: TransitionController? = null,
    orElse: NavController.() -> Boolean = {
        parent?.back() ?: false
    }
): Boolean = back(
    to = to,
    result = result,
    inclusive = inclusive,
    transitionData = TransitionData(
        contentTransform = transition,
        transitionController = transitionController
    ),
    orElse = orElse
)