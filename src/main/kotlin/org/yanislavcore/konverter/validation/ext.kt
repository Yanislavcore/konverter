package org.yanislavcore.konverter.validation

import org.yanislavcore.konverter.ContextDsl

@ContextDsl
inline infix fun <C : Any, K : Any?> ValidatorBuilder<C>.match(crossinline block: (K) -> Boolean): ValidationStage<C, K> =
    match(null, block)

@ContextDsl
inline fun <C : Any, K : Any?> ValidatorBuilder<C>.match(
    msg: String?,
    crossinline block: (K) -> Boolean
): ValidationStage<C, K> =
    { d ->
        if (!block(d)) {
            val finalMsg = msg ?: "Validation of the field '${this.name}' with value '$d' is failed"
            invalid(finalMsg)
        } else {
            success()
        }
    }