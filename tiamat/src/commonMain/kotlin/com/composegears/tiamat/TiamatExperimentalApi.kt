package com.composegears.tiamat

@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "This is an experimental Tiamat API, and it is likely to be changed in the future."
)
public annotation class TiamatExperimentalApi