package org.yanislavcore.konverter.validation

import kotlin.reflect.KProperty1

data class ValidationResult(
    val causedBy: List<InvalidStatus>? = null,
    val successful: Boolean = false
) {
    companion object {
        val SUCCESS = ValidationResult(null, true)
    }
}

typealias ValidationStage<C, T> = KProperty1<C, T?>.(T) -> ValidationStatus

internal data class ValidatorChildMeta<C>(
    val field: KProperty1<C, Any?>,
    val validator: Validator<Any>,
    val allowNullable: Boolean
)

internal data class ValidatorFieldMeta<C>(
    val field: KProperty1<C, Any?>,
    val validator: ValidationStage<C, Any?>
)

interface ValidationStatus

object ValidStatus : ValidationStatus

sealed class InvalidStatus(open val field: KProperty1<Any, Any?>) : ValidationStatus

data class InvalidFieldStatus(
    override val field: KProperty1<Any, Any?>,
    val description: String
) : InvalidStatus(field)

data class InvalidNestedObjectStatus(
    override val field: KProperty1<Any, Any?>,
    val causedBy: List<InvalidStatus>
) : InvalidStatus(field)