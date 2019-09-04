package org.yanislavcore.konverter

class JustConvertingStage<R>(value: R) : ConvertingStage<R> {
    private val result = ConvertingResult.success(value)
    override fun calculate(): ConvertingResult<R> = result
}