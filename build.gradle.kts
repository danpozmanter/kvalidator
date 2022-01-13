plugins {
    kotlin("jvm") version "1.6.10"
}

group = "kvalidator"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("test"))
}

testing {
    suites {
        // Configure the built-in test suite
        val test by getting(JvmTestSuite::class) {
            useKotlinTest()
        }
    }
}

