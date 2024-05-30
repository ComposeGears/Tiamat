package composegears.tiamat.example

import composegears.tiamat.sample.koin.KoinIntegrationScreen

actual val platformExamplesConfig = PlatformConfig(
    platformName = "iOS",
    availableScreens = listOf(
        PlatformConfig.PlatformDestination(
            name = "Koin (ViewModel/SharedViewModel)",
            destination = KoinIntegrationScreen
        )
    )
)