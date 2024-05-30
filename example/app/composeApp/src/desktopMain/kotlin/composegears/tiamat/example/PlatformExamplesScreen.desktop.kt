package composegears.tiamat.example

import composegears.tiamat.example.PlatformConfig.PlatformDestination
import composegears.tiamat.sample.koin.KoinIntegrationScreen

actual val platformExamplesConfig = PlatformConfig(
    platformName = "Desktop",
    availableScreens = listOf(
        PlatformDestination(
            name = "Koin (ViewModel/SharedViewModel)",
            destination = KoinIntegrationScreen
        )
    )
)