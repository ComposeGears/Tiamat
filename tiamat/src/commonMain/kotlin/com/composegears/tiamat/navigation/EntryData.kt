package com.composegears.tiamat.navigation

import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import kotlin.jvm.JvmName
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import androidx.savedstate.SavedState as SavedStateX

public sealed class EntryData<T : Any>(
) {
    public companion object {
        @Suppress("UNCHECKED_CAST")
        internal fun from(data: Any?): EntryData<Any>? = when (data) {
            null -> null
            is EntryData<*> -> data as EntryData<Any>
            is SavedStateX -> SerializedData(data)
            else -> Value(data)
        }

        public fun <T : Any> serializable(data: T, type: KType): EntryData<T> = SerializableData(data, type)

    }

    internal abstract val data: T?

    internal abstract fun toSavedState(): Any?
}

public inline fun <reified T : Any> serializable(data: T): EntryData<T> = EntryData.serializable(data, typeOf<T>())

@JvmName("toSerializable")
public inline fun <reified T : Any> T.serializable(): EntryData<T> = EntryData.serializable(this, typeOf<T>())

internal class Value<T : Any>(
    override val data: T?
) : EntryData<T>() {
    override fun toSavedState() = data
}

internal data class SerializedData<T : Any>(
    val savedState: SavedStateX,
) : EntryData<T>() {
    override val data: T? = null

    override fun toSavedState() = savedState

    @Suppress("UNCHECKED_CAST")
    fun tryDecode(type: KType): SerializableData<T>? = runCatching {
        SerializableData(
            data = decodeFromSavedState(deserializer = serializer(type) as KSerializer<T>, savedState = savedState),
            type = type
        )
    }.getOrNull()
}

internal data class SerializableData<T : Any>(
    override val data: T,
    val type: KType
) : EntryData<T>() {
    override fun toSavedState() = encodeToSavedState(serializer = serializer(type), value = data)
}