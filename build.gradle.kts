import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    kotlin("jvm") version "2.3.10"
    kotlin("plugin.serialization") version "2.3.10"
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
    testImplementation("org.junit.jupiter:junit-jupiter:5.14.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Testcontainers
    testImplementation("org.testcontainers:testcontainers:1.21.4")
    testImplementation("org.testcontainers:junit-jupiter:1.21.4")
    testImplementation("org.testcontainers:kafka:1.21.4")

    // AssertJ for assertions
    testImplementation("org.assertj:assertj-core:3.27.7")

    // AWS SDK for EventBridge
    implementation("aws.sdk.kotlin:eventbridge:1.6.14")
    implementation("aws.sdk.kotlin:sqs:1.6.14")
    implementation("aws.smithy.kotlin:http-client-engine-okhttp:1.6.1")

    // Kafka client
    implementation("org.apache.kafka:kafka-clients:3.9.1")

    // JSON serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.29")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.10.2")
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

