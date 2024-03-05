package com.composegear.navigation

import App
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import content.examples.model.DeeplinkData

class MainActivity : ComponentActivity() {

    private var deeplinkData by mutableStateOf<DeeplinkData?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        handleIntent(intent)

        setContent {
            App(
                deeplinkData = deeplinkData,
                onDeeplinkHandled = { deeplinkData = null }
            )
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent ?: return

        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val appLinkData = intent.data ?: return

        val categoryId = appLinkData.getQueryParameter("category_id").orEmpty()
        val productId = appLinkData.getQueryParameter("product_id").orEmpty()
        val title = appLinkData.getQueryParameter("title").orEmpty()

        deeplinkData = DeeplinkData(
            categoryId = categoryId,
            productId = productId,
            productName = title
        )
    }
}