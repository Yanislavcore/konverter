package org.yanislavcore.konverter

class ConverterValidationException(
    val reasons: List<ConverterValidationException>? = null,
    message: String = "",
    cause: Throwable? = null
) : RuntimeException(message, cause)

class ConverterException(
    val reasons: List<Throwable>,
    message: String = ""
) : RuntimeException(message)
