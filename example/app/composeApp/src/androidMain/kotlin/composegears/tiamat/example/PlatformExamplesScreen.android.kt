package composegears.tiamat.example

import composegears.tiamat.example.PlatformConfig.PlatformDestination
import composegears.tiamat.example.platform.AndroidViewLifecycleScreen
import composegears.tiamat.example.platform.DeeplinkScreen
import composegears.tiamat.example.platform.SavedStateScreen
import composegears.tiamat.sample.koin.KoinIntegrationScreen

actual val platformExamplesConfig = PlatformConfig(
    platformName = "Android",
    availableScreens = listOf(
        PlatformDestination(
            name = "Android SavedState",
            destination = SavedStateScreen
        ),
        PlatformDestination(
            name = "AndroidView + Lifecycle handle",
            destination = AndroidViewLifecycleScreen
        ),
        PlatformDestination(
            name = "Deeplink",
            destination = DeeplinkScreen
        ),
        PlatformDestination(
            name = "Koin (ViewModel/SharedViewModel)",
            destination = KoinIntegrationScreen
        )
    )
)