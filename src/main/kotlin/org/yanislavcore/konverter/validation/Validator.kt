package org.yanislavcore.konverter.validation

import org.yanislavcore.konverter.validation.ValidationResult.Companion.SUCCESS
import kotlin.reflect.KClass

class Validator<C : Any> internal constructor(
    private val klass: KClass<C>,
    private val fieldValidators: List<ValidatorFieldMeta<C>>,
    private val childrenValidators: List<ValidatorChildMeta<C>>
) {

    private fun doFieldValidation(value: C, failfast: Boolean): List<ValidationResult> =
        if (!failfast) {
            fieldValidators.mapNotNull { meta -> validateField(value, meta).takeIf { !it.successful } }
        } else {
            fieldValidators.asSequence()
                .mapNotNull { meta -> validateField(value, meta) }
                .firstOrNull { r -> !r.successful }
                ?.let { listOf(it) }
                ?: emptyList()
        }

    private fun validateField(obj: C, meta: ValidatorFieldMeta<C>): ValidationResult {
        val fieldValue = meta.field.get(obj)
        return meta.validator.invoke(meta.field, fieldValue)
    }

    private fun doChildrenValidation(value: C, failfast: Boolean): List<ValidationResult> =
        if (!failfast) {
            childrenValidators.mapNotNull { meta -> validateChild(value, failfast, meta).takeIf { !it.successful } }
        } else {
            childrenValidators.asSequence()
                .mapNotNull { meta -> validateChild(value, failfast, meta) }
                .firstOrNull { r -> !r.successful }
                ?.let { listOf(it) }
                ?: emptyList()
        }

    private fun validateChild(obj: C, failfast: Boolean, meta: ValidatorChildMeta<C>): ValidationResult {
        val fieldValue = meta.field.get(obj)
            ?: return if (meta.allowNullable) SUCCESS else ValidationResult("'${meta.field.name}' should be not null")
        return meta.validator.invoke(fieldValue, failfast)
    }

    operator fun invoke(value: C, failfast: Boolean = false): ValidationResult {
        val failedFields = doFieldValidation(value, failfast)
        if (failfast && failedFields.isNotEmpty()) {
            return ValidationResult(
                "Validation of object of class '${klass.simpleName}' is failed, caused by: ${failedFields.first().message}",
                failedFields
            )
        }
        val failedChildren = doChildrenValidation(value, failfast)
        if (failfast && failedChildren.isNotEmpty()) {
            return ValidationResult(
                "Validation of object of class '${klass.simpleName}' is failed, caused by: ${failedChildren.first().message}",
                failedChildren
            )
        }

        return if (failedFields.isEmpty() && failedChildren.isEmpty()) {
            SUCCESS
        } else {
            ValidationResult(
                "Validation of object of class '${klass.simpleName}' is failed, see subreasons",
                failedFields + failedChildren
            )
        }
    }

    fun validate(value: C, failfast: Boolean = false) = this(value, failfast)

}