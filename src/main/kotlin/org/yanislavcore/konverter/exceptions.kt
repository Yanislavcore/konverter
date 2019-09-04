package org.yanislavcore.konverter

class StageValidationException(
    message: String,
    cause: Throwable?
) : RuntimeException(message, cause)

class ValidationException(
    val reasons: List<StageValidationException>,
    message: String = ""
) : RuntimeException(message)

class ConverterException(
    val reasons: List<Throwable>,
    message: String = ""
) : RuntimeException(message)
