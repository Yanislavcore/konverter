# Konverter

[ ![Download](https://api.bintray.com/packages/yanislavcore/konverter/konverter/images/download.svg?version=0.2.0-rc.1) ](https://bintray.com/yanislavcore/konverter/konverter/0.2.0-rc.1/link)

Lightweight Kotlin library for painless and type-safe building and validation of immutable objects.

## Features

* Simple and painless builders for immutable objects
* Explicit and flexible builders
* Lazy calculations
* Validation during building
* Validation and building errors aggregation
* Lightweight and minimal-dependency (only `kotlin-stdlin` and `kotlin-reflect`)

## Setup

[Full setup info](https://bintray.com/yanislavcore/konverter/konverter)

### Add to repositories

Gradle 

```groovy
repositories {
    maven {
        url  "https://dl.bintray.com/yanislavcore/konverter" 
    }
}
```

Maven

```xml
<repositories>
    <repository>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
        <id>bintray-yanislavcore-konverter</id>
        <name>bintray</name>
        <url>https://dl.bintray.com/yanislavcore/konverter</url>
    </repository>
</repositories>
```

### Add dependency

Gradle
```
compile 'org.yanislavcore:konverter:0.2.0-rc.1'
```

Maven
```xml
<dependency>
  <groupId>org.yanislavcore</groupId>
  <artifactId>konverter</artifactId>
  <version>0.2.0-rc.1</version>
</dependency>
```

## Usage

```kotlin
import org.yanislavcore.konverter.ConverterValidationException
import org.yanislavcore.konverter.Konverter
import org.yanislavcore.konverter.mapping.MappingBuilder.DslHelper.invalid
import java.math.BigDecimal
import java.util.*


data class BarData(val first: String, val second: UUID)
data class Foo(
    val bar1: String,
    val bar2: Int,
    val bar3: BigDecimal,
    val bar4: BarData,
    val bar5: BarData?
)

fun build(input: Map<String, String>): Foo = Konverter.convert {

    val bar1 = lazyMapping { input["bar1"] ?: invalid("'bar1' should be not null") }

    Foo::bar1 with bar1

    Foo::bar2 with bar1.mapRight {
        it.toIntOrNull() ?: invalid("'bar1' should be int")
    }

    Foo::bar3 with lazyMapping {
        val s = input["bar3"] ?: invalid("'bar3' should be not null")
        s.toBigDecimalOrNull() ?: invalid("'bar3' should be float number")
    }

    Foo::bar4 with Konverter.lazyConvert {
        BarData::first with lazyMapping { input["bar4"] ?: invalid("'bar4' should be not null") }
        BarData::second with just(UUID.randomUUID())
    }

    Foo::bar5 withNullable just(null as BarData?)
}

fun main() {
    val input = mapOf(
        "bar1" to "42",
        "bar3" to "27.123",
        "bar4" to "barfoo"
    )

    val result: Foo = build(input)
    assert(42 == result.bar2)
    assert(BigDecimal("27.123") == result.bar3)

    val invalidInput = input.minus("bar1")
    val failedResult: ConverterValidationException? = try {
        build(invalidInput)
        null
    } catch (e: ConverterValidationException) {
        e
    }

    assert(failedResult != null)
    assert(failedResult!!.reasons!!.first().message == "'bar1' should be not null")
}
``` 

## Building

`./gradlew clean build`