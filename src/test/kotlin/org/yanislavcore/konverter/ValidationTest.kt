package org.yanislavcore.konverter

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldContainSame
import org.amshove.kluent.shouldNotBeNull
import org.yanislavcore.konverter.validation.InvalidFieldStatus
import org.yanislavcore.konverter.validation.InvalidNestedObjectStatus
import org.yanislavcore.konverter.validation.ValidatorBuilder
import org.yanislavcore.konverter.validation.match
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.reflect.KProperty1

class ValidationTest : StringSpec({
    val validatorBaseBuilder: ValidatorBuilder<TestData>.() -> Unit = {
        TestData::first should { value ->
            if (value.isNotBlank()) {
                success()
            } else {
                invalid("Should not be empty")
            }
        }
        TestData::first shouldBeNotNullAnd { value ->
            if (value.isNotBlank()) {
                success()
            } else {
                invalid("Should not be empty")
            }
        }
        TestData::second should { value: String? ->
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
            result.causedBy.shouldBeNull()
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

        val expectedInvalidFields = listOf(TestData::second, TestData::fourth, SubTestData::firstSub)

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
            result.causedBy.shouldNotBeNull()
            result.causedBy!!.size shouldBe 3
            val invalidFields = ArrayList<KProperty1<Any, Any?>>()
            result.causedBy!!.forEach { r ->
                when (r) {
                    is InvalidFieldStatus -> {
                        invalidFields.add(r.field)
                    }
                    is InvalidNestedObjectStatus -> {
                        r.causedBy.forEach { invalidFields.add(it.field) }
                    }
                }
            }
            invalidFields shouldContainSame expectedInvalidFields

            val failfastResult = validator(d, failfast = true)
            failfastResult.successful shouldBe false
            failfastResult.causedBy.shouldNotBeNull()
            failfastResult.causedBy!!.size shouldBe 1
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
}