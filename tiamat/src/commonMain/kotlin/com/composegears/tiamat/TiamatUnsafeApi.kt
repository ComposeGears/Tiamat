package com.composegears.tiamat

@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "Use of this API is unsafe and may lead to unexpected behavior or crashes. Use with caution."
)
public annotation class TiamatUnsafeApi