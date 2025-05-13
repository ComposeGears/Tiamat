package composegears.tiamat.example.ui.core

import com.composegears.tiamat.navigation.NavDestination

data class AppFeature(
    val name: String,
    val description: String,
    val destination: NavDestination<*>
)