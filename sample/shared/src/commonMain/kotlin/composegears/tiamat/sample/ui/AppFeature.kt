package composegears.tiamat.sample.ui

import com.composegears.tiamat.navigation.NavDestination

data class AppFeature(
    val name: String,
    val description: String,
    val destination: NavDestination<*>
)