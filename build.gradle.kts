import com.jfrog.bintray.gradle.BintrayExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.50"
    id("com.jfrog.bintray") version "1.8.4"
    `maven-publish`
    `java-library`
}

group = "org.yanislavcore"
version = "0.1.0"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    api(kotlin("stdlib-jdk8"))
    api(kotlin("reflect"))
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.3.2")
    testImplementation("org.amshove.kluent:kluent:1.53")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform { }
}


val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val javadocJar by tasks.registering(Jar::class) {
    dependsOn.add(tasks.javadoc)
    archiveClassifier.set("javadoc")
    from(tasks.javadoc)
}

publishing {
    repositories {
        maven {
            // change to point to your repo, e.g. http://my.org/repo
            url = uri("$buildDir/repo")
        }
    }
    publications {
        register("konverter", MavenPublication::class) {
            this.artifactId = "konverter"
            this.version = version
            from(components["java"])
            artifact(sourcesJar.get())
            artifact(javadocJar.get())
            pom {
                name.set("konverter")
                description.set("Simple Kotlin mapping and validation library")
                url.set("https://github.com/Yanislavcore/konverter")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://github.com/Yanislavcore/konverter/blob/master/LICENSE")
                    }
                }
                developers {
                    developer {
                        id.set("yanislavcore")
                        name.set("Yanislav Kornev")
                        email.set("yanislavcore@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/Yanislavcore/konverter")
                    developerConnection.set("scm:git:https://github.com/Yanislavcore/konverter")
                    url.set("https://github.com/Yanislavcore/konverter")
                }
            }
        }
    }
}

fun findProperty(s: String) = project.findProperty(s) as String?

bintray {
    user = findProperty("bintrayUser")
    key = findProperty("bintrayApiKey")
    publish = true
    setPublications("konverter")
    pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
        repo = "konverter"
        name = "konverter"
        userOrg = "yanislavcore"
        websiteUrl = "https://github.com/Yanislavcore/konverter/"
        vcsUrl = "https://github.com/Yanislavcore/konverter/"
        setLabels("kotlin")
        setLicenses("MIT")
    })
}