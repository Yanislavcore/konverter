package org.yanislavcore.konverter.validation

import kotlin.reflect.KProperty1

data class ValidationResult(
    val message: String,
    val subReasons: List<ValidationResult>? = null,
    val successful: Boolean = false
) {
    companion object {
        val SUCCESS = ValidationResult("", null, true)
    }
}

typealias ValidationStage<C, T> = (KProperty1<C, T?>, T) -> ValidationResult

internal data class ValidatorChildMeta<C>(
    val field: KProperty1<C, Any?>,
    val validator: Validator<Any>,
    val allowNullable: Boolean
)

internal data class ValidatorFieldMeta<C>(
    val field: KProperty1<C, Any?>,
    val validator: ValidationStage<C, Any?>
)