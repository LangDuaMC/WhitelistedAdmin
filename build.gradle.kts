import org.apache.tools.ant.filters.FixCrLfFilter
import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream

plugins {
    kotlin("jvm") version "1.5.10"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("com.github.gmazzo.buildconfig") version "3.0.0"
    id("org.jlleitschuh.gradle.ktlint") version "11.3.2"
}

group = "net.langdua"
version = "1.0.0"

repositories {
    maven(url = "https://papermc.io/repo/repository/maven-public/")
    maven(url = "https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/") {
        name = "sonatype-oss-snapshots1"
    }
    mavenCentral()
}

// val minecraft_version: String = "1.16.5"

dependencies {
    // PaperMC Dependency
    compileOnly("com.destroystokyo.paper", "paper-api", "1.16.5-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.3")
    implementation("net.kyori:adventure-text-minimessage:4.13.1")

    // Add your dependencies here
    // Examples
    // implementation("io.ktor", "ktor-client", "1.4.0") // Would be shaded into the final jar
    // compileOnly("io.ktor", "ktor-client", "1.4.0") // Only used on compile time
    implementation(kotlin("stdlib"))
}

buildConfig {
    className("BuildConfig")
    packageName("$group.$name")
    val commit = getGitHash()
    val branch = getGitBranch()
    buildConfigField("String", "GIT_COMMIT", "\"$commit\"")
    buildConfigField("String", "GIT_BRANCH", "\"$branch\"")
}

fun getGitHash(): String {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine("git", "rev-parse", "--short", "HEAD")
        standardOutput = stdout
    }
    return stdout.toString("UTF-8").trim()
}

fun getGitBranch(): String {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine("git", "rev-parse", "--abbrev-ref", "HEAD")
        standardOutput = stdout
    }
    return stdout.toString("UTF-8").trim()
}

tasks {
    processResources {
        filter(FixCrLfFilter::class)
        filter(ReplaceTokens::class, "tokens" to mapOf("version" to project.version))
        filteringCharset = "UTF-8"
    }
    jar {
        // Disabled, because we use the shadowJar task for building our jar
        enabled = false
    }
    build {
        dependsOn(shadowJar)
    }
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}
