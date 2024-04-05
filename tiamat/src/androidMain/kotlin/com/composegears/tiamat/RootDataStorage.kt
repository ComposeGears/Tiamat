@file:Suppress("MatchingDeclarationName")

package com.composegears.tiamat

import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel

/**
 * Internal view model
 *
 * Holds in-memory data storage
 */
internal class RootStorageModel : ViewModel() {
    val storage: NavControllersStorage = NavControllersStorage()
}

/**
 * @return platform root data storage object
 */
@Composable
internal fun rememberRootDataStore(): NavControllersStorage {
    val context = LocalContext.current
    val activity = remember(context) {
        var ctx = context
        while (ctx !is ComponentActivity) {
            ctx = (ctx as ContextWrapper).baseContext
        }
        ctx
    }
    val storageModel by activity.viewModels<RootStorageModel>()
    return storageModel.storage
}