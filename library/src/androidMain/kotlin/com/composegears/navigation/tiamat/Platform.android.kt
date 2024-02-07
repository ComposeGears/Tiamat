package com.composegears.navigation.tiamat

import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
    val storage = DataStorage()
}

/**
 * @return platform root data storage object
 */
@Composable
internal actual fun rootDataStore(): DataStorage {
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

@Composable
internal actual fun PlatformBackHandler(enabled: Boolean, onBackEvent: () -> Unit) {
    BackHandler(enabled, onBackEvent)
}