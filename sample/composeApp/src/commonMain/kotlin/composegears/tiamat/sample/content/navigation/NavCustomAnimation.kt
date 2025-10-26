package composegears.tiamat.sample.content.navigation

import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.compose.*
import com.composegears.tiamat.navigation.NavDestination
import composegears.tiamat.sample.ui.AppButton
import composegears.tiamat.sample.ui.AppTheme
import composegears.tiamat.sample.ui.Screen
import composegears.tiamat.sample.ui.ScreenInfo
import composegears.tiamat.sample.ui.VSpacer

val NavCustomAnimation by navDestination(ScreenInfo()) {
    Screen("Custom animation") {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val nc = rememberNavController(
                key = "Custom Animation nav controller",
                startDestination = NavCustomAnimationScreen1,
            )
            Navigation(
                navController = nc,
                destinations = arrayOf(
                    NavCustomAnimationScreen1,
                    NavCustomAnimationScreen2,
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp)) // prevent render out of bounds during slide animation
            )
        }
    }
}

// we define type here to bypass circular initialization issue
private val NavCustomAnimationScreen1: NavDestination<Unit> by navDestination {
    val nc = navController()
    Surface {
        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Screen 1", style = MaterialTheme.typography.headlineMedium)
                VSpacer()
                AppButton(
                    "Next (scale in + out)",
                    modifier = Modifier.widthIn(min = 400.dp),
                    endIcon = Icons.AutoMirrored.Default.KeyboardArrowRight,
                    onClick = {
                        nc.navigate(
                            NavCustomAnimationScreen2,
                            transition = scaleIn() togetherWith scaleOut()
                        )
                    }
                )
                AppButton(
                    "Next (slide)",
                    modifier = Modifier.widthIn(min = 400.dp),
                    endIcon = Icons.AutoMirrored.Default.KeyboardArrowRight,
                    onClick = {
                        nc.navigate(
                            NavCustomAnimationScreen2,
                            transition = navigationSlideInOut(true)
                        )
                    }
                )
                AppButton(
                    "Next (slide from bottom)",
                    modifier = Modifier.widthIn(min = 400.dp),
                    endIcon = Icons.AutoMirrored.Default.KeyboardArrowRight,
                    onClick = {
                        nc.navigate(
                            NavCustomAnimationScreen2,
                            transition = navigationSlideInFromBottom()
                        )
                    }
                )
                AppButton(
                    "Next (fade)",
                    modifier = Modifier.widthIn(min = 400.dp),
                    endIcon = Icons.AutoMirrored.Default.KeyboardArrowRight,
                    onClick = {
                        nc.navigate(
                            NavCustomAnimationScreen2,
                            transition = navigationFadeInOut()
                        )
                    }
                )
            }
        }
    }
}

private val NavCustomAnimationScreen2 by navDestination {
    val nc = navController()
    Surface {
        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Screen 2", style = MaterialTheme.typography.headlineMedium)
                VSpacer()
                AppButton(
                    "Back (scale in + out)",
                    modifier = Modifier.widthIn(min = 400.dp),
                    startIcon = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                    onClick = { nc.back(transition = scaleIn() togetherWith scaleOut()) }
                )
                AppButton(
                    "Back (slide)",
                    modifier = Modifier.widthIn(min = 400.dp),
                    startIcon = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                    onClick = { nc.back(transition = navigationSlideInOut(false)) }
                )
                AppButton(
                    "Back (slide down)",
                    modifier = Modifier.widthIn(min = 400.dp),
                    startIcon = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                    onClick = { nc.back(transition = navigationSlideOutToBottom()) }
                )
            }
        }
    }
}

@Preview
@Composable
private fun NavCustomAnimationPreview() = AppTheme {
    TiamatPreview(destination = NavCustomAnimation)
}
