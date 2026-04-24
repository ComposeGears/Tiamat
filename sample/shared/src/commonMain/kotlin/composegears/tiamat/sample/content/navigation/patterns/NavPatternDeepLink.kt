package composegears.tiamat.sample.content.navigation.patterns

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.TiamatExperimentalApi
import com.composegears.tiamat.compose.*
import com.composegears.tiamat.navigation.DeepLink
import com.composegears.tiamat.navigation.NavDestination.Companion.toNavEntry
import composegears.tiamat.sample.ui.*

private const val NESTED_NC_KEY = "deeplink-pd-nested"

@OptIn(TiamatExperimentalApi::class)
private val AppDeepLink = DeepLink {
    bind("shop") {
        element(DeepLinkShop.toNavEntry())
    }
    bind("shop/product/{id}", "shop/product?id={id}") { params ->
        element(DeepLinkProduct.toNavEntry(navArgs = params[0]))
    }
    bind(
        "shop/product/{pid}/feedback/{fid}",
        "shop/product?id={pid}/feedback?id={fid}",
    ) { params ->
        element(DeepLinkProduct.toNavEntry(navArgs = params[0]))
        navController(NESTED_NC_KEY)
        element(DeepLinkFeedback.toNavEntry(navArgs = params[1]))
    }
}

private val DeepLinks = listOf(
    "app://x/shop" to "shop",
    "app://x/shop/product/42" to "shop/product/42",
    "app://x/shop/product/42/feedback/34" to "shop/product/42/feedback/34",
    "app://x/shop/product?id=42" to "shop / product?id=42",
    "app://x/shop/product?id=42/feedback?id=34" to "shop/product?id=42/feedback?id=34",
)

@OptIn(TiamatExperimentalApi::class)
val NavPatternDeepLink by navDestination(ScreenInfo()) {
    Screen("Deep links") {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text(
                text = "Tap any deep link below — it'll be parsed by `DeepLink` and dispatched " +
                    "to the nested NavController via `route(...)`.",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            VSpacer()
            val nestedNc = rememberNavController(
                key = "deeplink-host-nc",
                startDestination = DeepLinkHome,
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DeepLinks.forEach { (uri, label) ->
                    AppButton(
                        text = label,
                        onClick = { nestedNc.route(AppDeepLink.parse(uri)) }
                    )
                }
            }
            VSpacer()
            Navigation(
                navController = nestedNc,
                destinations = arrayOf(
                    DeepLinkHome,
                    DeepLinkShop,
                    DeepLinkProduct,
                    DeepLinkFeedback,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            )
        }
    }
}

private val DeepLinkHome by navDestination {
    DeepLinkScreenContent("Home", "Use the buttons above to dispatch a deep link.")
}

private val DeepLinkShop by navDestination {
    DeepLinkScreenContent("Shop", "List of products would go here.")
}

private val DeepLinkProduct by navDestination<String> {
    val id = navArgs()
    val nc = navController()
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Product #$id", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            Text("Nested deep links open a child NavController inside this screen.")
            VSpacer()
            // host for the nested NavController used by feedback deep links
            val nestedNc = rememberNavController(
                key = NESTED_NC_KEY,
                startDestination = DeepLinkProductTab,
            )
            Navigation(
                navController = nestedNc,
                destinations = arrayOf(
                    DeepLinkProductTab,
                    DeepLinkFeedback,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            )
        }
    }
}

private val DeepLinkProductTab by navDestination {
    DeepLinkScreenContent("Product overview", "Default tab inside product details.")
}

private val DeepLinkFeedback by navDestination<String> {
    val id = navArgs()
    val nc = navController()
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Feedback #$id", style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
private fun DeepLinkScreenContent(title: String, subtitle: String) {
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.headlineMedium)
            VSpacer(8.dp)
            Text(subtitle, textAlign = TextAlign.Center)
        }
    }
}

@Preview
@Composable
private fun NavPatternDeepLinkPreview() = AppTheme {
    TiamatPreview(destination = NavPatternDeepLink)
}
