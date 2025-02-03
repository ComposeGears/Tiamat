@file:Suppress("MatchingDeclarationName")

package com.composegears.tiamat

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalInspectionMode
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
    if (LocalInspectionMode.current) {
        // as for android preview we use in memory storage
        return remember { NavControllersStorage() }
    } else {
        val activity = LocalActivity.current as? ComponentActivity ?: error("Activity not found")
        val storageModel by activity.viewModels<RootStorageModel>()
        return storageModel.storage
    }
}