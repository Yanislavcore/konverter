package org.yanislavcore.konverter

data class ConvertingResult<R>(
    private val exception: Throwable?,
    private val value: R?
) {

    @Throws(IllegalStateException::class)
    fun success(): R {
        if (isFailed()) {
            throw IllegalStateException("Result was exceptional. See cause.", exception!!)
        }
        @Suppress("UNCHECKED_CAST")
        return value as R
    }

    @Throws(IllegalStateException::class)
    fun fail(): Throwable {
        if (isSuccess()) {
            throw IllegalStateException("Result was successful")
        }
        @Suppress("UNCHECKED_CAST")
        return exception!!
    }

    fun isSuccess() = exception == null
    fun isFailed() = exception != null

    companion object {
        fun <R> success(value: R) = ConvertingResult(null, value)
        fun <R> fail(exception: Throwable) = ConvertingResult<R>(exception, null)
    }
}