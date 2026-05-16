import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    kotlin("jvm") version "2.3.21"
    kotlin("plugin.serialization") version "2.3.21"
    application
}
group = "io.specmatic.async"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    // JUnit 5
    testImplementation("org.junit.jupiter:junit-jupiter:5.14.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Testcontainers
    testImplementation("org.testcontainers:testcontainers:1.21.4")
    testImplementation("org.testcontainers:junit-jupiter:1.21.4")
    testImplementation("org.testcontainers:kafka:1.21.4")

    // AssertJ for assertions
    testImplementation("org.assertj:assertj-core:3.27.7")

    // AWS SDK for EventBridge
    implementation("aws.sdk.kotlin:eventbridge:1.6.75")
    implementation("aws.sdk.kotlin:sqs:1.6.72")
    implementation("aws.smithy.kotlin:http-client-engine-okhttp:1.6.14")

    // Kafka client
    implementation("org.apache.kafka:kafka-clients:3.9.2")

    // JSON serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.32")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.11.0")
}

application {
    mainClass.set("io.specmatic.async.MainKt")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
        events(
            TestLogEvent.STARTED,
            TestLogEvent.PASSED,
            TestLogEvent.FAILED,
            TestLogEvent.SKIPPED
        )
    }
}

kotlin {
    jvmToolchain(17)
}

