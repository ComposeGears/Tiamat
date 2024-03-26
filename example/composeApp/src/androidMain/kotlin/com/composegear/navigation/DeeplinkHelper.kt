package com.composegear.navigation

import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class DeeplinkData(
    val categoryId: String,
    val productId: String,
    val productName: String
)

class DeepLinkController {
    var deeplink by mutableStateOf<DeeplinkData?>(null)
        private set

    fun onIntent(intent: Intent) {
        val appLinkData = intent.data ?: return

        val categoryId = appLinkData.getQueryParameter("category_id").orEmpty()
        val productId = appLinkData.getQueryParameter("product_id").orEmpty()
        val title = appLinkData.getQueryParameter("title").orEmpty()

        deeplink = DeeplinkData(
            categoryId = categoryId,
            productId = productId,
            productName = title
        )
    }

    fun clearDeepLink() {
        deeplink = null
    }
}