package org.yanislavcore.konverter.mapping

/**
 * Result of calculation can be either exceptional or successful. Never can be both.
 */
class ConvertingResult<R> private constructor(
    private val exception: Throwable?,
    private val value: R?
) {

    /**
     * Returns success result of throws [IllegalStateException]
     */
    @Throws(IllegalStateException::class)
    fun success(): R {
        if (isFailed()) {
            throw IllegalStateException("Result was exceptional. See cause.", exception!!)
        }
        @Suppress("UNCHECKED_CAST")
        return value as R
    }

    /**
     * Returns exceptional result of throws [IllegalStateException]
     */
    @Throws(IllegalStateException::class)
    fun fail(): Throwable {
        if (isSuccess()) {
            throw IllegalStateException("Result was successful")
        }
        @Suppress("UNCHECKED_CAST")
        return exception!!
    }

    /**
     * Returns true if result is successful
     */
    fun isSuccess() = exception == null

    /**
     * Returns true if result is exceptional
     */
    fun isFailed() = exception != null


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ConvertingResult<*>) return false

        if (exception != other.exception) return false
        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = exception?.hashCode() ?: 0
        result = 31 * result + (value?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "ConvertingResult(exception=$exception, value=$value)"
    }

    companion object {
        /**
         * Creates successful result
         */
        fun <R> success(value: R) = ConvertingResult(null, value)

        /**
         * Creates exceptional result
         */
        fun <R> fail(exception: Throwable) =
            ConvertingResult<R>(exception, null)
    }


}