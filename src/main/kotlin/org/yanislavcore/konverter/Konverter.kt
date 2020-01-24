package org.yanislavcore.konverter

import org.yanislavcore.konverter.validation.Validator
import org.yanislavcore.konverter.validation.ValidatorBuilder
import kotlin.reflect.KClass

/**
 * Performs conversions and creates validators.
 */
object Konverter {
    /**
     * Performs validation and building of instance of specified class.
     * @param klass - Kotlin class to build
     * @param failFast - if false Konverter will aggregate all exception and validation errors,
     * otherwise will throw exception after first fail
     * @param builder - builder of instance
     * @throws ConverterValidationException in case of any validation errors during building
     * @throws ConverterException in case of any non-validation errors during building
     * @throws IllegalStateException in case of any builder creation errors
     * @return built instance of specified class
     */
    @Throws(ConverterValidationException::class, ConverterException::class, IllegalStateException::class)
    inline fun <T : Any> convertTo(
        klass: KClass<T>,
        failFast: Boolean = false,
        builder: MappingBuilder<T>.() -> Unit
    ): T =
        MappingBuilder(klass)
            .apply(builder)
            .build(failFast)


    /**
     * Performs validation and building of instance of specified class.
     * @param failFast - if false Konverter will aggregate all exception and validation errors,
     * otherwise will throw exception after first fail
     * @param builder - builder of instance
     * @throws ConverterValidationException in case of any validation errors during building
     * @throws ConverterException in case of any non-validation errors during building
     * @throws IllegalStateException in case of any builder creation errors
     * @return built instance of specified class
     */
    @Throws(ConverterValidationException::class, ConverterException::class, IllegalStateException::class)
    inline fun <reified T : Any> convert(failFast: Boolean = false, builder: MappingBuilder<T>.() -> Unit): T =
        convertTo(T::class, failFast, builder)


    /**
     * Creates lazy builder for validation and building of instance of specified class.
     * @param klass - Kotlin class to build
     * @param failFast - if false Konverter will aggregate all exception and validation errors,
     * otherwise will throw exception after first fail
     * @param builder - builder of instance
     * @throws ConverterValidationException in case of any validation errors during building
     * @throws ConverterException in case of any non-validation errors during building
     * @throws IllegalStateException in case of any builder creation errors
     * @return built instance of specified class
     */
    @Throws(ConverterValidationException::class, ConverterException::class)
    inline fun <T : Any> lazyConvertTo(
        klass: KClass<T>,
        failFast: Boolean = false,
        crossinline builder: MappingBuilder<T>.() -> Unit
    ): ConvertingStage<T> =
        LazyConvertingStage {
            MappingBuilder(klass)
                .apply(builder)
                .build(failFast)
        }

    /**
     * Creates lazy builder for validation and building of instance of specified class.
     * @param failFast - if false Konverter will aggregate all exception and validation errors,
     * otherwise will throw exception after first fail
     * @param builder - builder of instance
     * @throws ConverterValidationException in case of any validation errors during building
     * @throws ConverterException in case of any non-validation errors during building
     * @throws IllegalStateException in case of any builder creation errors
     * @return built instance of specified class
     */
    @Throws(ConverterValidationException::class, ConverterException::class)
    inline fun <reified T : Any> lazyConvert(
        failFast: Boolean = false,
        crossinline builder: MappingBuilder<T>.() -> Unit
    ):
            ConvertingStage<T> = lazyConvertTo(T::class, failFast, builder)


    inline fun <reified T : Any> validator(builder: ValidatorBuilder<T>.() -> Unit): Validator<T> =
        ValidatorBuilder(T::class)
            .apply(builder)
            .build()

}