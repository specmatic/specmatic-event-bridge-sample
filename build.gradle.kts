import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.serialization") version "2.2.21"
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
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Testcontainers
    testImplementation("org.testcontainers:testcontainers:1.19.3")
    testImplementation("org.testcontainers:junit-jupiter:1.19.3")
    testImplementation("org.testcontainers:kafka:1.19.3")

    // AssertJ for assertions
    testImplementation("org.assertj:assertj-core:3.24.2")

    // AWS SDK for EventBridge
    implementation("aws.sdk.kotlin:eventbridge:1.3.90")
    implementation("aws.sdk.kotlin:sqs:1.5.113")
    implementation("aws.smithy.kotlin:http-client-engine-okhttp:1.3.25")

    // Kafka client
    implementation("org.apache.kafka:kafka-clients:3.6.1")

    // JSON serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.14")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.7.3")
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

