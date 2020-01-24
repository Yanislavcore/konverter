package org.yanislavcore.konverter

import io.kotlintest.properties.Gen
import io.kotlintest.properties.shrinking.Shrinker
import io.kotlintest.properties.shrinking.StringShrinker
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldNotBeNull
import org.yanislavcore.konverter.validation.ValidatorBuilder
import org.yanislavcore.konverter.validation.match
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.random.Random

class ValidationTest : StringSpec({
    val validatorBaseBuilder: ValidatorBuilder<TestData>.() -> Unit = {
        TestData::first should { _, value ->
            if (value.isNotBlank()) {
                success()
            } else {
                invalid("Should not be empty")
            }
        }
        TestData::first shouldBeNotNullAnd { _, value ->
            if (value.isNotBlank()) {
                success()
            } else {
                invalid("Should not be empty")
            }
        }
        TestData::second should { _, value: String? ->
            if (value == null) {
                success()
            } else {
                invalid("Should be null")
            }
        }
        TestData::second.shouldBeNull()
        repeat(10) {
            TestData::fifth.shouldBeValidWith(false) {
                SubTestData::firstSub should match {
                    it != null && it >= BigDecimal.ZERO
                }
            }
        }
    }

    "should return success every time" {
        val validator = Konverter.validator<TestData> {
            validatorBaseBuilder()
            TestData::fourth.shouldBeNull()
        }
        repeat(100) { i ->
            val d1 = TestData(
                "abv".repeat(i % 10 + 1),
                null,
                i * 17,
                null,
                SubTestData(BigDecimal(i * 29), BigInteger.valueOf(31L * i))
            )
            val result = validator(d1)
            result.successful shouldBe true
            result.subReasons.shouldBeNull()
        }
    }

    "should fail with with 3 errors" {
        val validator = Konverter.validator<TestData> {
            TestData::second.shouldBeNull()
            TestData::fourth.shouldBeNotNull()
            TestData::fifth shouldBeValidWith {
                SubTestData::firstSub.shouldBeNull()
            }
        }

        repeat(100) { i ->
            val d = TestData(
                "abc123".repeat(i % 10 + 1),
                "bfg".repeat(i % 10 + 1),
                i * 17,
                null,
                SubTestData(BigDecimal.ZERO, BigInteger.ZERO)
            )
            val result = validator(d)
            result.successful shouldBe false
            result.subReasons.shouldNotBeNull()
            result.subReasons!!.size shouldBe 3
            var subreasonsNum = 0
            result.subReasons!!.forEach { r ->
                subreasonsNum += r.subReasons?.size ?: 0
                r.successful shouldBe false
                r.subReasons?.any { it.successful }?.shouldBe(false)
            }
            subreasonsNum shouldBe 1

            val failfastResult = validator(d, failfast = true)
            failfastResult.successful shouldBe false
            failfastResult.subReasons.shouldNotBeNull()
            failfastResult.subReasons!!.size shouldBe 1
        }
    }

}) {
    data class TestData(
        val first: String,
        val second: String?,
        val third: Int?,
        val fourth: SubTestData?,
        val fifth: SubTestData
    )

    data class SubTestData(
        val firstSub: BigDecimal?,
        val secondSub: BigInteger
    )

    companion object {
        fun notEmptyStringGen(): Gen<String> = object : Gen<String> {
            val literals =
                listOf("\nabc\n123\n", "\u006c\u0069b/\u0062\u002f\u006d\u0069nd/m\u0061x\u002e\u0070h\u0070")

            override fun constants(): Iterable<String> = literals
            override fun random(): Sequence<String> = generateSequence { nextPrintableString(Random.nextInt(10, 100)) }
            override fun shrinker(): Shrinker<String>? = StringShrinker
        }
    }
}