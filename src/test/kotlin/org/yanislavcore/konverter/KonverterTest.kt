package org.yanislavcore.konverter

import io.kotlintest.properties.assertAll
import io.kotlintest.shouldThrowExactly
import io.kotlintest.specs.StringSpec
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldMatchAllWith

class KonverterTest : StringSpec({
    "Should set value to right field" {
        assertAll { f: String, s: String, t: Int, fo: String ->
            val result = Konverter.convertTo(TestData::class) {
                TestData::one with lazyMapping { f }
                TestData::second with lazyMapping { s }
                TestData::third with lazyMapping { t }
                TestData::forth withNonNull lazyMapping { fo }
            }

            result.one shouldBe f
            result.second shouldBe s
            result.third shouldEqual t
            result.forth shouldBe fo
        }
    }

    "Should successfully build without nullable field" {
        assertAll { f: String, s: String, t: Int ->
            val result = Konverter.convertTo(TestData::class) {
                TestData::one with lazyMapping { f }
                TestData::second with lazyMapping { s }
                TestData::third with lazyMapping { t }
            }

            result.one shouldBe f
            result.second shouldBe s
            result.third shouldEqual t
            result.forth shouldBe null
        }
    }

    "Should calculate all fields by default and throw Invalid exception with all invalid reasons" {
        assertAll { o: String ->
            var calculated = 0
            val result = shouldThrowExactly<ValidationException> {
                Konverter.convertTo(TestData::class) {
                    TestData::one with lazyMapping<String> { invalid("test") }
                    TestData::second with lazyMapping<String> { invalid("test") }
                    TestData::third with lazyMapping<Int> { invalid("test") }
                    TestData::forth withNonNull lazyMapping {
                        calculated++
                        o
                    }
                }
            }

            calculated shouldEqual 1
            result.reasons.size shouldEqual 3
            result.reasons.shouldMatchAllWith {
                it.message == "test"
            }
        }
    }

    "Should calculate only first field if failfast=true" {
        assertAll { o: String ->
            val firstInvalidMsg = "FIRST_MSG"
            var calculated = 0
            val result = shouldThrowExactly<ValidationException> {
                Konverter.convertTo(TestData::class, true) {
                    TestData::one with lazyMapping<String> {
                        calculated++
                        invalid(firstInvalidMsg)
                    }
                    TestData::second with lazyMapping<String> {
                        calculated++
                        invalid("test")
                    }
                    TestData::third with lazyMapping<Int> {
                        calculated++
                        invalid("test")
                    }
                    TestData::forth withNonNull lazyMapping {
                        calculated++
                        o
                    }
                }
            }

            calculated shouldEqual 1
            result.reasons.size shouldEqual 1
            result.reasons.shouldMatchAllWith {
                it.message == firstInvalidMsg
            }
        }
    }

    "Should work ok with chaining" {
        assertAll { f: Int ->
            var firstCalculated = 0
            var secondCalculated = 0
            var thirdCalculated = 0
            val result = Konverter.convertTo(TestData::class) {
                val first = lazyMapping {
                    firstCalculated++
                    f
                }
                val second = first.mapRight {
                    secondCalculated++
                    (it + 1).toString()
                }
                val third = first.combine(second) { f, s ->
                    thirdCalculated++
                    f + s.toInt()
                }
                val forth = third
                    .map<String> {
                        throw RuntimeException()
                    }
                    .recover {
                        "12"
                    }
                TestData::one with first.mapRight { it.toString() }
                TestData::second with second
                TestData::third with third
                TestData::forth withNonNull forth
            }

            listOf(firstCalculated, secondCalculated, thirdCalculated).shouldMatchAllWith { it == 1 }
            result.one shouldEqual f.toString()
            result.second shouldEqual (f + 1).toString()
            result.third shouldEqual (f + f + 1)
            result.forth shouldEqual "12"
        }
    }

    "Should work ok with just chaining" {
        assertAll { f: String, t: Int ->
            val postfix = "1235"
            val result: TestData = Konverter.convert {
                val first = just(f)
                TestData::one with first
                TestData::second with first.mapRight { it + postfix }
                TestData::third with just(t)
            }

            result.one shouldEqual f
            result.second shouldEqual (f + postfix)
            result.third shouldEqual t
        }
    }
}) {

    data class TestData(
        val one: String,
        val second: String,
        val third: Int,
        val forth: String?
    )
}