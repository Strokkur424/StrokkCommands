import de.chojo.PublishDataExtension
import net.kyori.indra.licenser.spotless.IndraSpotlessLicenserExtension

plugins {
    id("java-library")
    id("maven-publish")
    id("de.chojo.publishdata") version "1.4.0"
    id("net.kyori.indra.licenser.spotless") version "3.1.3"
}

group = "net.strokkur"
version = "1.2.0"

fun IndraSpotlessLicenserExtension.apply() {
    licenseHeaderFile(rootProject.file("HEADER"))
}

fun PublishDataExtension.apply() {
    useEldoNexusRepos(true)
    publishComponent("java")
}

publishData.apply()

fun PublishingExtension.apply() {
    repositories {
        maven {
            authentication {
                credentials(PasswordCredentials::class) {
                    username = System.getenv("NEXUS_USERNAME")
                    password = System.getenv("NEXUS_PASSWORD")
                }
            }

            name = "EldoNexus"
            setUrl(publishData.getRepository())
        }
    }

    publications.create<MavenPublication>("maven") {
        publishData.configurePublication(this)
    }
}

subprojects {
    plugins.apply("java-library")
    plugins.apply("net.kyori.indra.licenser.spotless")

    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
    
    version = rootProject.version
    group = rootProject.group

    indraSpotlessLicenser.apply()

    if (name.contains("processor") || name.contains("annotations")) {
        plugins.apply("de.chojo.publishdata")
        plugins.apply("maven-publish")
        publishData.apply()
        publishing.apply()
    }
}

indraSpotlessLicenser.apply()

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}