package content.examples

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.*
import content.examples.common.TextButton


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

@OptIn(ExperimentalComposeUiApi::class)
val MasterExampleRoot by navDestination<Unit> {
    val model = rememberViewModel { Model() }

    val wi = LocalWindowInfo.current
    val isWideScreen by derivedStateOf { wi.containerSize.width > 600 }

    val nc = rememberNavController(
        destinations = arrayOf(ListScreen, MasterItemDetailsScreen, NothingSelectedScreen)
    )
    LaunchedEffect(Unit) {
        if (nc.current == null) nc.navigate(ListScreen, model)
    }

    LaunchedEffect(isWideScreen) {
        if (isWideScreen) {
            nc.editBackStack { clear() }
            if (nc.current == ListScreen) nc.replace(NothingSelectedScreen, transition = navigationNone())
        } else {
            nc.editBackStack { add(0, ListScreen, model) }
            if (nc.current == NothingSelectedScreen) nc.back(transition = navigationNone())
        }
    }

    val nav = remember {
        movableContentOf {
            Navigation(
                navController = nc,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    if (isWideScreen) Row(Modifier.fillMaxSize()) {
        Box(Modifier.width(400.dp).fillMaxHeight()) {
            ListScreenContent(model) {
                nc.replace(MasterItemDetailsScreen, it)
            }
        }
        nav()
    } else Box(Modifier.fillMaxSize()) {
        nav()
    }
}

@Composable
private fun ListScreenContent(model: Model, onItemClick: (MasterItem) -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        LazyColumn {
            items(model.items, key = { it.name }) {
                Row {
                    var c by rememberSaveable { mutableStateOf(false) }
                    Checkbox(c, onCheckedChange = { c = it })
                    TextButton(it.name) {
                        onItemClick(it)
                    }
                }
            }
        }
    }
}

private val ListScreen by navDestination<Model> {
    val nc = navController()
    val model = navArgs()
    ListScreenContent(model) {
        nc.navigate(MasterItemDetailsScreen, it)
    }
}

private val NothingSelectedScreen by navDestination<Unit> {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("no items selected")
    }
}

private val MasterItemDetailsScreen by navDestination<MasterItem?> {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val args = navArgsOrNull()
        if (args != null) Text("Item: ${args.name}")
        else Text("no items selected")
    }
}