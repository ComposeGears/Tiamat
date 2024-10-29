package composegears.tiamat.example

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.*
import composegears.tiamat.example.ui.core.*

data class MasterItem(
    val name: String
)

class Model : TiamatViewModel() {
    val items = listOf(
        MasterItem("Item 0"),
        MasterItem("Item 1"),
        MasterItem("Item 2"),
        MasterItem("Item 3"),
        MasterItem("Item 4"),
        MasterItem("Item 5"),
        MasterItem("Item 6"),
        MasterItem("Item 7"),
        MasterItem("Item 8"),
        MasterItem("Item 9"),
    )
}

val TwoPaneResizableRoot by navDestination<Unit>(webPathExtension()) {
    SimpleScreen("2 Pane") {
        BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val model = rememberViewModel { Model() }
            val isWideScreen by derivedStateOf { maxWidth > 600.dp }
            var activeItem by remember { mutableStateOf<MasterItem?>(null) }
            // list content
            val listComposable = remember {
                movableContentOf {
                    ListScreenContent(model) { activeItem = it }
                }
            }
            // details content
            val detailsContent = remember(activeItem) {
                movableContentOf {
                    ItemDetailContent(activeItem)
                }
            }
            // placements
            if (isWideScreen) Row {
                Box(Modifier.width(300.dp).fillMaxHeight()) {
                    listComposable()
                }
                VerticalDivider()
                Box(Modifier.fillMaxSize()) {
                    detailsContent()
                }
            } else {
                NavBackHandler(activeItem != null) {
                    activeItem = null
                }
                AnimatedContent(
                    targetState = activeItem,
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                    transitionSpec = { navigationFadeInOut() },
                    contentKey = { activeItem?.name ?: "#nothing" }
                ) {
                    if (it == null) listComposable()
                    else Column {
                        if (activeItem != null) BackButton { activeItem = null }
                        detailsContent()
                    }
                }
            }
        }
    }
}

@Composable
private fun ListScreenContent(model: Model, onItemClick: (MasterItem) -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        LazyColumn {
            items(model.items, key = { it.name }) {
                Row {
                    TextButton(it.name) {
                        onItemClick(it)
                    }
                }
            }
        }
    }
}

@Composable
fun ItemDetailContent(item: MasterItem?) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (item == null) Text("Nothing selected")
        else {
            val nc = rememberNavController(
                startDestination = DetailRootScreen,
                startDestinationNavArgs = item,
                destinations = arrayOf(DetailRootScreen, FullDetailsRootScreen)
            )
            Navigation(nc, Modifier.fillMaxSize())
        }
    }
}

val DetailRootScreen by navDestination<MasterItem> {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val item = navArgs()
        val nc = navController()
        Column {
            Text("Item: ${item.name}")
            NextButton { nc.navigate(FullDetailsRootScreen, item) }
        }
    }
}
val FullDetailsRootScreen by navDestination<MasterItem> {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val item = navArgs()
        val nc = navController()
        Column {
            Text("Full item: ${item.name}")
            BackButton { nc.back() }
        }
    }
}