package org.yanislavcore.konverter

import kotlin.reflect.KClass

object Konverter {
    fun <T : Any> converTo(
        klass: KClass<T>,
        failFast: Boolean = false,
        builder: MappingBuilder<T>.() -> Unit
    ): T =
        MappingBuilder(klass)
            .apply(builder)
            .build(failFast)
}