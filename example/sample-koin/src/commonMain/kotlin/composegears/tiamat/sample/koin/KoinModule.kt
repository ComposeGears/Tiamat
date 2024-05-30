package composegears.tiamat.sample.koin

import com.composegears.tiamat.koin.tiamatViewModelOf
import composegears.tiamat.sample.koin.viewmodel.KoinDetailViewModel
import composegears.tiamat.sample.koin.viewmodel.SharedViewModel
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