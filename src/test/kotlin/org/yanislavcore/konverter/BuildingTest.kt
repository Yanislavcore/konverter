package org.yanislavcore.konverter

import io.kotlintest.specs.StringSpec

class BuildingTest : StringSpec({
    "just should work" {
        val map = mapOf(
            "1" to "sdsfdsg",
            "2" to "second",
            "3" to "third",
            "4" to null
        )
        val result = Konverter.converTo(Test::class) {
            Test::one bindWith lazyMapping { map["1"] ?: invalid("") }

            Test::second bindWith lazyMapping { map["2"] ?: invalid("") }

            Test::third bindWith lazyMapping { map["3"] ?: invalid("") }
        }

    }
}) {

    data class Test(
        val one: String,
        val second: String,
        val third: String,
        val forth: String?
    )
}