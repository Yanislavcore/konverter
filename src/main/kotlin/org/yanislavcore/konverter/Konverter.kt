package org.yanislavcore.konverter

import kotlin.reflect.KClass

object Konverter {
    @Throws(ValidationException::class, ConverterException::class)
    inline fun <T : Any> convertTo(
        klass: KClass<T>,
        failFast: Boolean = false,
        builder: MappingBuilder<T>.() -> Unit
    ): T =
        MappingBuilder(klass)
            .apply(builder)
            .build(failFast)

    @Throws(ValidationException::class, ConverterException::class)
    inline fun <reified T : Any> convert(failFast: Boolean = false, builder: MappingBuilder<T>.() -> Unit): T =
        convertTo(T::class, failFast, builder)
}