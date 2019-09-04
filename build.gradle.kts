import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.50"
}

group = "org.yanislavcore"
version = "0.1"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.3.2")
    testImplementation("org.amshove.kluent:kluent:1.53")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform { }
}