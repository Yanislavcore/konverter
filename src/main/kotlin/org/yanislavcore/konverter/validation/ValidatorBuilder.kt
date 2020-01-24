package org.yanislavcore.konverter.validation

import org.yanislavcore.konverter.ContextDsl
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class ValidatorBuilder<C : Any> constructor(private val mappedClass: KClass<C>) {
    private val fieldValidators: MutableList<ValidatorFieldMeta<C>> = ArrayList()
    private val childrenValidators: MutableList<ValidatorChildMeta<C>> = ArrayList()

    @ContextDsl
    infix fun <K : Any?> KProperty1<C, K>.should(validator: ValidationStage<C, K>) {
        val meta = ValidatorFieldMeta(this, validator as ValidationStage<C, Any?>)
        fieldValidators.add(meta)
    }

    @ContextDsl
    inline infix fun <K : Any> KProperty1<C, K?>.shouldBeNotNullAnd(crossinline validator: ValidationStage<C, K>) {
        val field = this
        should { f, v ->
            if (v == null) {
                invalid("Field ${f.name} should be not null")
            } else {
                validator(field, v)
            }
        }
    }

    @ContextDsl
    fun <K : Any?> KProperty1<C, K?>.shouldBeNotNull() {
        val validator: ValidationStage<C, K?> = { f, value ->
            if (value == null) {
                invalid("Field ${f.name} should be not null")
            } else {
                success()
            }
        }
        val meta = ValidatorFieldMeta(this, validator as ValidationStage<C, Any?>)
        fieldValidators.add(meta)
    }

    @ContextDsl
    fun <K : Any?> KProperty1<C, K?>.shouldBeNull() {
        val validator: ValidationStage<C, K?> = { f, value ->
            if (value != null) {
                invalid("Field ${f.name} should be not null")
            } else {
                success()
            }
        }
        val meta = ValidatorFieldMeta(this, validator as ValidationStage<C, Any?>)
        fieldValidators.add(meta)
    }

    @ContextDsl
    infix fun <K : Any> KProperty1<C, K>.shouldBeValidWith(validator: Validator<K>) {
        val meta = ValidatorChildMeta(this, validator as Validator<Any>, false)
        childrenValidators.add(meta)
    }

    @ContextDsl
    inline infix fun <reified K : Any> KProperty1<C, K>.shouldBeValidWith(
        builder: ValidatorBuilder<K>.() -> Unit
    ) {
        ValidatorBuilder(K::class)
            .apply(builder)
            .build()
            .let { shouldBeValidWith(it) }
    }

    @ContextDsl
    fun <K : Any> KProperty1<C, K?>.shouldBeValidWith(nullable: Boolean, validator: Validator<K>) {
        val meta = ValidatorChildMeta(this, validator as Validator<Any>, nullable)
        childrenValidators.add(meta)
    }

    @ContextDsl
    inline fun <reified K : Any> KProperty1<C, K?>.shouldBeValidWith(
        nullable: Boolean,
        builder: ValidatorBuilder<K>.() -> Unit
    ) {
        ValidatorBuilder(K::class)
            .apply(builder)
            .build()
            .let { shouldBeValidWith(nullable, it) }
    }


    @ContextDsl
    fun success() = ValidationResult.SUCCESS

    @ContextDsl
    fun invalid(msg: String) = ValidationResult(message = msg)

    fun build(): Validator<C> = Validator(mappedClass, fieldValidators, childrenValidators)

}