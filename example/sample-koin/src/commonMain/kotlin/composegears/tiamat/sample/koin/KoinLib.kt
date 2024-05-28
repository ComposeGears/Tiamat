package composegears.tiamat.sample.koin

import org.koin.core.context.startKoin

object KoinLib {
    fun start() {
        startKoin {
            modules(koinIntegrationModule)
        }
    }
}