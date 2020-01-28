package org.yanislavcore.konverter.validation

import org.yanislavcore.konverter.ContextDsl
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class ValidatorBuilder<C : Any> constructor(private val mappedClass: KClass<C>) {
    private val fieldValidators: MutableList<ValidatorFieldMeta<C>> = ArrayList()
    private val childrenValidators: MutableList<ValidatorChildMeta<C>> = ArrayList()

    @ContextDsl
    infix fun <K : Any?> KProperty1<C, K>.should(validator: ValidationStage<C, K>) {
        @Suppress("UNCHECKED_CAST")
        val meta = ValidatorFieldMeta(this, validator as ValidationStage<C, Any?>)
        fieldValidators.add(meta)
    }

    @ContextDsl
    inline infix fun <K : Any> KProperty1<C, K?>.shouldBeNotNullAnd(crossinline validator: ValidationStage<C, K>) {
        val field = this
        should { v ->
            if (v == null) {
                invalid("Field ${this.name} should be not null")
            } else {
                validator(field, v)
            }
        }
    }

    @ContextDsl
    fun <K : Any?> KProperty1<C, K?>.shouldBeNotNull() {
        val validator: ValidationStage<C, K?> = { value ->
            if (value == null) {
                invalid("Field ${this.name} should be not null")
            } else {
                success()
            }
        }
        @Suppress("UNCHECKED_CAST")
        val meta = ValidatorFieldMeta(this, validator as ValidationStage<C, Any?>)
        fieldValidators.add(meta)
    }

    @ContextDsl
    fun <K : Any?> KProperty1<C, K?>.shouldBeNull() {
        val validator: ValidationStage<C, K?> = { value ->
            if (value != null) {
                invalid("Field ${this.name} should be not null")
            } else {
                success()
            }
        }
        @Suppress("UNCHECKED_CAST")
        val meta = ValidatorFieldMeta(this, validator as ValidationStage<C, Any?>)
        fieldValidators.add(meta)
    }

    @ContextDsl
    infix fun <K : Any> KProperty1<C, K>.shouldBeValidWith(validator: Validator<K>) {
        @Suppress("UNCHECKED_CAST")
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
        @Suppress("UNCHECKED_CAST")
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
    fun <T> KProperty1<C, T?>.success() = ValidStatus

    @Suppress("UNCHECKED_CAST")
    @ContextDsl
    fun <T> KProperty1<C, T?>.invalid(msg: String) = InvalidFieldStatus(this as KProperty1<Any, Any?>, msg)

    fun build(): Validator<C> = Validator(mappedClass, fieldValidators, childrenValidators)

}