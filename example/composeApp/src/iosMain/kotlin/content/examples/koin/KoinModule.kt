package content.examples.koin

import com.composegears.tiamat.koin.tiamatViewModelOf
import content.examples.SharedViewModel
import org.koin.dsl.module

val koinIntegrationModule = module {
    tiamatViewModelOf(::KoinDetailViewModel)
    tiamatViewModelOf(::SharedViewModel)

    /**
     * or with old Koin syntax
     *
     * ```
     * tiamatViewModel { param -> DetailViewModel(params = param.get()) }
     * ```
     */
}