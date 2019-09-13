package org.yanislavcore.konverter

class ValidationException(
    val reasons: List<ValidationException>? = null,
    message: String = "",
    cause: Throwable? = null
) : RuntimeException(message, cause)

class ConverterException(
    val reasons: List<Throwable>,
    message: String = ""
) : RuntimeException(message)
