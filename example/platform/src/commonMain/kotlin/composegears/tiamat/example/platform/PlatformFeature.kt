package composegears.tiamat.example.platform

import com.composegears.tiamat.NavDestination

data class PlatformFeature(
    val name: String,
    val description: String,
    val destination: NavDestination<*>
)