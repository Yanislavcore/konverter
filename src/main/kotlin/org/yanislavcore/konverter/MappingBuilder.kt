package org.yanislavcore.konverter

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.primaryConstructor

class MappingBuilder<RESULT : Any> constructor(private val mappedClass: KClass<RESULT>) {
    private val propertyToBuilder: MutableMap<KProperty1<RESULT, *>, ConvertingStage<*>> = LinkedHashMap()
    infix fun <T : Any> KProperty1<RESULT, T>.with(builder: ConvertingStage<T>) {
        if (propertyToBuilder.put(this, builder) != null) {
            error("Already mapped. Failed to create builder")
        }
    }

    infix fun <T : Any> KProperty1<RESULT, T?>.withNonNull(builder: ConvertingStage<T>) {
        if (propertyToBuilder.put(this, builder) != null) {
            error("Already mapped. Failed to create builder")
        }
    }

    infix fun <T : Any> KProperty1<RESULT, T?>.withNullable(builder: ConvertingStage<T?>) {
        if (propertyToBuilder.put(this, builder) != null) {
            error("Already mapped. Failed to create builder")
        }
    }

    fun <T> just(value: T): ConvertingStage<T> = JustConvertingStage(value)

    inline fun <T> lazyMapping(crossinline initializer: MappingContext.() -> T): ConvertingStage<T> =
        LazyConvertingStage { MappingContext.initializer() }

    @Suppress("unused")
    fun MappingContext.invalid(msg: String, cause: Exception? = null): Nothing =
        throw StageValidationException(msg, cause)

    @Throws(ValidationException::class, ConverterException::class)
    fun build(failFast: Boolean = false): RESULT {
        val calculationResult = propertyToBuilder.map { (k, v) ->
            val result = v.calculate()

            if (failFast && result.isFailed()) {
                val ex = result.fail()
                if (ex is StageValidationException) {
                    throw ValidationException(listOf(ex), "Validation failed (failFast=true)")
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
            if (fails.any { it !is StageValidationException }) {
                throw ConverterException(fails, "Internal error during conversion (failFast=false)")
            } else {
                @Suppress("UNCHECKED_CAST")
                val invalidParamFails = fails as List<StageValidationException>
                throw ValidationException(invalidParamFails, "Validation failed (failFast=true)")
            }
        }

        val propNameToValue = calculationResult
            .associate { (k, v) -> k to v.success() }

        return mappedClass.primaryConstructor!!.callBy(
            mappedClass.primaryConstructor!!.parameters.associateWith { propNameToValue[it.name] }
        )
    }

    object MappingContext
}