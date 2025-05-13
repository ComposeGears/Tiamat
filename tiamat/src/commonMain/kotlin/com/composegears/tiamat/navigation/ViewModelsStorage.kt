package com.composegears.tiamat.navigation

public class ViewModelsStorage {
    private val internalViewModels = mutableMapOf<String, TiamatViewModel>()
    public val viewModels: Map<String, TiamatViewModel> = internalViewModels

    @Suppress("UNCHECKED_CAST")
    internal fun <Model : TiamatViewModel> get(key: String, factory: () -> Model): Model =
        internalViewModels.getOrPut(key, factory) as Model

    internal fun clear() {
        internalViewModels.onEach { it.value.close() }
        internalViewModels.clear()
    }
}