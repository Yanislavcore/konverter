package org.yanislavcore.konverter.validation

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class Validator<C : Any> internal constructor(
    private val klass: KClass<C>,
    private val fieldValidators: List<ValidatorFieldMeta<C>>,
    private val childrenValidators: List<ValidatorChildMeta<C>>
) {

    private fun doFieldValidation(value: C, failfast: Boolean): List<InvalidStatus> =
        if (!failfast) {
            fieldValidators.mapNotNull { meta ->
                val status = validateField(value, meta)
                if (status is InvalidStatus) {
                    status
                } else {
                    null
                }
            }
        } else {
            fieldValidators.asSequence()
                .mapNotNull { meta ->
                    val status = validateField(value, meta)
                    if (status is InvalidStatus) {
                        status
                    } else {
                        null
                    }
                }
                .firstOrNull()
                ?.let { listOf(it) }
                ?: emptyList()
        }

    private fun validateField(obj: C, meta: ValidatorFieldMeta<C>): ValidationStatus {
        val fieldValue = meta.field.get(obj)
        return meta.validator.invoke(meta.field, fieldValue)
    }

    private fun doChildrenValidation(value: C, failfast: Boolean): List<InvalidStatus> =
        if (!failfast) {
            childrenValidators.mapNotNull { meta ->
                val status = validateChild(value, failfast, meta)
                if (status is InvalidStatus) {
                    status
                } else {
                    null
                }
            }
        } else {
            childrenValidators.asSequence()
                .mapNotNull { meta ->
                    val status = validateChild(value, failfast, meta)
                    if (status is InvalidStatus) {
                        status
                    } else {
                        null
                    }
                }
                .firstOrNull()
                ?.let { listOf(it) }
                ?: emptyList()
        }

    private fun validateChild(obj: C, failfast: Boolean, meta: ValidatorChildMeta<C>): ValidationStatus {
        @Suppress("UNCHECKED_CAST")
        val fieldValue = meta.field.get(obj)
            ?: return if (meta.allowNullable) ValidStatus else InvalidFieldStatus(
                meta.field as KProperty1<Any, Any?>,
                "'${meta.field.name}' should be not null"
            )
        val result = meta.validator.invoke(fieldValue, failfast)
        if (result.successful) {
            return ValidStatus
        } else {
            @Suppress("UNCHECKED_CAST")
            return InvalidNestedObjectStatus(meta.field as KProperty1<Any, Any?>, result.causedBy!!)
        }
    }

    operator fun invoke(value: C, failfast: Boolean = false): ValidationResult {
        val failedFields = doFieldValidation(value, failfast)
        if (failfast && failedFields.isNotEmpty()) {
            return ValidationResult(failedFields, false)
        }
        val failedChildren = doChildrenValidation(value, failfast)
        if (failfast && failedChildren.isNotEmpty()) {
            return ValidationResult(failedChildren, false)
        }

        return if (failedFields.isEmpty() && failedChildren.isEmpty()) {
            ValidationResult.SUCCESS
        } else {
            ValidationResult(failedFields + failedChildren)
        }
    }

    fun validate(value: C, failfast: Boolean = false) = this(value, failfast)

}