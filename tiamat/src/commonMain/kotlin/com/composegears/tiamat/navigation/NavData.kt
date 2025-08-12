package com.composegears.tiamat.navigation

/**
 * Marker interface for navigation data.
 *
 * Implement this interface to indicate that a class is intended to be used as serializable navigation arguments or results.
 * Classes implementing [NavData] can be encoded and decoded for navigation state persistence.
 *
 * **Usage:**
 * - Classes implementing [NavData] must be annotated with `@Serializable`.
 * - The appropriate Kotlin serialization plugin must be applied to the Gradle module for serialization to work.
 */
public interface NavData