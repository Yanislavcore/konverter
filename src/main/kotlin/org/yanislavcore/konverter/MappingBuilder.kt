package org.yanislavcore.konverter

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.primaryConstructor

/**
 * Universal generic type safe builder.
 * @param RESULT - type that should be built
 */
class MappingBuilder<RESULT : Any> constructor(private val mappedClass: KClass<RESULT>) {
    private val propertyToBuilder: MutableMap<KProperty1<RESULT, *>, ConvertingStage<*>> = LinkedHashMap()


    /**
     * Binds field of target class with non-null field [builder].
     * @param builder - specific field builder
     * @throws IllegalStateException if field is already mapped
     */
    @Throws(IllegalStateException::class)
    infix fun <T : Any> KProperty1<RESULT, T?>.with(builder: ConvertingStage<T>) {
        if (propertyToBuilder.put(this, builder) != null) {
            error("Already mapped. Failed to create builder")
        }
    }

    /**
     * Binds field of target class with nullable field [builder].
     * @param builder - specific field builder
     * @throws IllegalStateException if field is already mapped
     */
    @Throws(IllegalStateException::class)
    infix fun <T : Any?> KProperty1<RESULT, T?>.withNullable(builder: ConvertingStage<T>) {
        if (propertyToBuilder.put(this, builder) != null) {
            error("Already mapped. Failed to create builder")
        }
    }

    /**
     * Creates builder that just supplies [value] (constant).
     * @param value - value to supply
     * @return constant builder that just returns [value].
     */
    fun <T> just(value: T): ConvertingStage<T> = JustConvertingStage(value)

    /**
     * Creates lazy builder.
     * [initializer] will be executed only once during building.
     * @param initializer function that supplies result to value
     * @return creates builder.
     */
    inline fun <T> lazyMapping(crossinline initializer: () -> T): ConvertingStage<T> =
        LazyConvertingStage { initializer() }

    /**
     * Just throws [ValidationException] with specified message and cause
     * @param msg message in exception
     * @param cause cause of invalidation, nullable
     * @throws ValidationException
     */
    @Suppress("unused")
    fun invalid(msg: String, cause: Exception? = null): Nothing =
        throw ValidationException(message = msg, cause = cause)

    /**
     * Calculates all specified field builders and builds instance of class.
     * @param failFast if false Konverter will aggregate all exception and validation errors,
     * otherwise will throw exception after first fail
     * @throws ValidationException in case of any validation errors during building
     * @throws ConverterException in case of any non-validation errors during building
     * @return built instance of specified class
     */
    @Throws(ValidationException::class, ConverterException::class)
    fun build(failFast: Boolean = false): RESULT {
        val calculationResult = propertyToBuilder.map { (k, v) ->
            val result = v.calculate()

            if (failFast && result.isFailed()) {
                val ex = result.fail()
                if (ex is ValidationException) {
                    throw ValidationException(ex.reasons, "Validation failed (failFast=true). Cause: '${ex.message}'")
                } else {
                    throw ConverterException(listOf(ex), "Internal error during conversion (failFast=true)")
                }
            }
            k.name to result
        }

        if (!failFast && calculationResult.any { (_, v) -> v.isFailed() }) {
            val fails = calculationResult
                .filter { (_, v) -> v.isFailed() }
                .map { (_, v) -> v.fail() }
            if (fails.any { it !is ValidationException }) {
                throw ConverterException(fails, "Internal error during conversion (failFast=false)")
            } else {
                @Suppress("UNCHECKED_CAST")
                val invalidParamFails = fails as List<ValidationException>
                throw ValidationException(invalidParamFails, "Validation failed (failFast=true)")
            }
        }

        val propNameToValue = calculationResult
            .associate { (k, v) -> k to v.success() }

        return mappedClass.primaryConstructor!!.callBy(
            mappedClass.primaryConstructor!!.parameters.associateWith { propNameToValue[it.name] }
        )
    }
}