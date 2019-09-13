package org.yanislavcore.konverter

import io.kotlintest.shouldThrowExactly
import io.kotlintest.specs.StringSpec
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldEqual

class ConvertingResultTest : StringSpec({
    "If exception set should be failed" {
        val subject = ConvertingResult.fail<String>(IllegalStateException())

        subject.isFailed() shouldBe true
        subject.isSuccess() shouldBe false
        subject.fail() shouldBeInstanceOf IllegalStateException::class
        shouldThrowExactly<IllegalStateException> {
            subject.success()
        }
    }
    "If exception set to null should be successful" {
        val subject = ConvertingResult.success("foo")

        subject.isFailed() shouldBe false
        subject.isSuccess() shouldBe true
        shouldThrowExactly<IllegalStateException> {
            subject.fail()
        }
        subject.success() shouldEqual "foo"
    }

    "If both set to null should be successful" {
        val subject = ConvertingResult.success<String?>(null)

        subject.isFailed() shouldBe false
        subject.isSuccess() shouldBe true
        shouldThrowExactly<IllegalStateException> {
            subject.fail()
        }
        subject.success() shouldEqual null
    }
})