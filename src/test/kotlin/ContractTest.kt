package io.specmatic.async

import io.specmatic.async.test.ApplicationTestRunner
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIf
import org.testcontainers.containers.ComposeContainer
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import org.testcontainers.containers.BindMode
import java.io.File
import java.time.Duration

@Testcontainers
@EnabledIf(value = "isNonCIOrLinux", disabledReason = "Run only on Linux in CI; all platforms allowed locally")
class ContractTest {

    companion object {
        @JvmStatic
        fun isNonCIOrLinux(): Boolean = System.getenv("CI") != "true" || System.getProperty("os.name").lowercase().contains("linux")

        private val DOCKER_COMPOSE_FILE = File("docker-compose.yml")

        private lateinit var infrastructure: ComposeContainer
        private lateinit var application: ApplicationTestRunner

        @JvmStatic
        @BeforeAll
        fun setup() {
            println("Starting infrastructure via docker-compose...")

            // Start infrastructure using docker-compose
            infrastructure = ComposeContainer(DOCKER_COMPOSE_FILE)
                .withLocalCompose(true)

            infrastructure.start()

            println("Infrastructure started via docker-compose")

            // Wait for services to be fully ready
            Thread.sleep(20000)

            // Initialize AWS resources (EventBridge, SQS)
            ApplicationTestRunner.initializeAwsResources()

            // Create reports directory if it doesn't exist
            File("./build/reports/specmatic").mkdirs()

            Thread.sleep(2000)

            // Start the application
            application = ApplicationTestRunner()
            application.start()
        }

        @JvmStatic
        @AfterAll
        fun tearDown() {
            // Stop the application
            if (::application.isInitialized) {
                application.close()
            }

            // Stop infrastructure
            if (::infrastructure.isInitialized) {
                infrastructure.stop()
            }
        }
    }

    @Test
    fun `run contract test for EventBridge to Kafka bridge`() {
        println("Starting Specmatic contract tests...")

        // Setup Specmatic container with host network mode
        val specmaticContainer = GenericContainer(DockerImageName.parse("specmatic/specmatic-async-core:latest"))
            .withCommand("test")
            .withFileSystemBind(
                "./specmatic.yaml",
                "/usr/src/app/specmatic.yaml",
                BindMode.READ_ONLY
            )
            .withFileSystemBind(
                "./spec",
                "/usr/src/app/spec",
                BindMode.READ_ONLY
            )
            .withFileSystemBind(
                "./build/reports/specmatic",
                "/usr/src/app/build/reports/specmatic",
                BindMode.READ_WRITE
            )
            .withNetworkMode("host")
            .withStartupTimeout(Duration.ofMinutes(5))
            .withLogConsumer { print(it.utf8String) }
            .waitingFor(Wait.forLogMessage(".*(Failed:|Success).*", 1)
                .withStartupTimeout(Duration.ofMinutes(3)))

        try {
            // Start the Specmatic container
            specmaticContainer.start()

            // Wait for tests to complete
            Thread.sleep(2000)

            // Check the logs for test results
            val logs = specmaticContainer.logs

            println("=".repeat(60))
            println("Specmatic Test Results")
            println("=".repeat(60))
            println(logs)
            println("=".repeat(60))

            // Assert that tests passed
            assertThat(logs)
                .withFailMessage("Specmatic tests failed. Check logs above for details.")
                .contains("Failed: 0")
        } finally {
            specmaticContainer.stop()
        }
    }
}

