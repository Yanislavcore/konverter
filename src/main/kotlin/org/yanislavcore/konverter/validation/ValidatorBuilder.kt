package org.yanislavcore.konverter.validation

import org.yanislavcore.konverter.ContextDsl
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class ValidatorBuilder<C : Any> constructor(private val mappedClass: KClass<C>) {
    private val fieldValidators: MutableList<ValidatorFieldMeta<C>> = ArrayList()
    private val childrenValidators: MutableList<ValidatorChildMeta<C>> = ArrayList()

    @ContextDsl
    infix fun <K : Any?> KProperty1<C, K?>.with(validator: ValidationStage<C, K>) {
        val meta = ValidatorFieldMeta(this, validator as ValidationStage<C, Any?>)
        fieldValidators.add(meta)
    }

    @ContextDsl
    inline fun <K : Any> KProperty1<C, K?>.withNotNull(crossinline validator: ValidationStage<C, K>) {
        with { f, v ->
            if (v == null) {
                invalid("Field ${f.name} should be not null")
            } else {
                validator(f, v)
            }
        }
    }

    @ContextDsl
    fun <K : Any> KProperty1<C, K?>.subValidator(nullable: Boolean, validator: Validator<K>) {
        val meta = ValidatorChildMeta(this, validator as Validator<Any>, nullable)
        childrenValidators.add(meta)
    }

    @ContextDsl
    inline fun <reified K : Any> KProperty1<C, K?>.subValidator(
        nullable: Boolean,
        builder: ValidatorBuilder<K>.() -> Unit
    ) {
        ValidatorBuilder(K::class)
            .apply(builder)
            .build()
            .let { subValidator(nullable, it) }
    }


    @ContextDsl
    fun success() = ValidationResult.SUCCESS

    @ContextDsl
    fun invalid(msg: String) = ValidationResult(message = msg)

    inline fun <K : Any?> simpleValidation(crossinline block: (K?) -> Boolean): ValidationStage<C, K> = { field, d ->
        if (!block(d)) {
            ValidationResult(message = "Validation of the field '${field.name}' with value '$d' is failed")
        } else {
            ValidationResult.SUCCESS
        }
    }

    fun build(): Validator<C> = Validator(mappedClass, fieldValidators, childrenValidators)

}