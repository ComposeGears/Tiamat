package content.examples.koin

import com.composegears.tiamat.koin.tiamatViewModelOf
import org.koin.dsl.module

val koinIntegrationModule = module {
    tiamatViewModelOf(::KoinDetailViewModel)
    tiamatViewModelOf(::KoinSharedViewModel)

    /**
     * or with old Koin syntax
     *
     * ```
     * tiamatViewModel { param -> DetailViewModel(params = param.get()) }
     * ```
     */
}