package content.examples.platform.examples

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.*
import content.examples.common.BackButton
import content.examples.common.NextButton
import content.examples.common.SimpleScreen
import content.examples.common.TextBody
import content.examples.model.DeeplinkData

/**
 * To run sample from adb, run in terminal:
 *
 * adb shell am start -W -a android.intent.action.VIEW -d "tiamat://deeplink?category_id=20202\&product_id=92\&title=Awesome\ product"
 *
 */

val DeeplinkScreen by navDestination<Unit> {
    val deeplinkData = freeArgs<DeeplinkData>()

    val deeplinkNavController = rememberNavController(
        startDestination = ShopScreen,
        destinations = arrayOf(ShopScreen, CategoryScreen, DetailScreen)
    )

    LaunchedEffect(deeplinkData) {
        deeplinkData ?: return@LaunchedEffect

        with(deeplinkNavController) {
            editBackStack {
                clear()
                add(ShopScreen)
                add(CategoryScreen, deeplinkData.categoryId)
            }
            replace(
                dest = DetailScreen,
                navArgs = DetailParams(deeplinkData.productName, deeplinkData.productId),
            )
        }
    }

    Navigation(modifier = Modifier.fillMaxSize(), navController = deeplinkNavController)
}

val ShopScreen by navDestination<Unit> {
    val navController = navController()

    SimpleScreen("Shop screen") {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Welcome to the Shop")
            NextButton(onClick = { navController.navigate(CategoryScreen) })
            BackButton(onClick = navController::back)
        }
    }
}

val CategoryScreen by navDestination<String?> {
    val categoryId = navArgsOrNull()
    val navController = navController()

    SimpleScreen("Category screen") {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextBody("categoryId: $categoryId")
            NextButton(onClick = { navController.navigate(DetailScreen) })
            BackButton(onClick = navController::back)
        }
    }
}

data class DetailParams(
    val productName: String,
    val productId: String
)

val DetailScreen by navDestination<DetailParams?> {
    val detailParams = navArgsOrNull()
    val navController = navController()

    SimpleScreen("Product screen") {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextBody("productName: ${detailParams?.productName}")
            TextBody("productId: ${detailParams?.productId}")
            BackButton(onClick = navController::back)
        }
    }
}