package org.yanislavcore.konverter

/**
 * Calculates mapping stages lazily.
 * Once calculated - same result is used for all following calculations.
 * Is NOT thread safe.
 */
class LazyConvertingStage<R> constructor(private val initializer: () -> R) : ConvertingStage<R> {
    private var result: ConvertingResult<R>? = null

    override fun calculate(): ConvertingResult<R> {
        if (result == null) {
            result = try {
                ConvertingResult.success(initializer())
            } catch (e: Throwable) {
                ConvertingResult.fail(e)
            }
        }

        return result!!
    }
}