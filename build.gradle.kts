import net.kyori.indra.licenser.spotless.IndraSpotlessLicenserExtension

plugins {
    id("java-library")
    id("net.kyori.indra.licenser.spotless") version "3.1.3"
}

group = "net.strokkur"
version = "1.2.0"

fun IndraSpotlessLicenserExtension.apply() {
    licenseHeaderFile(rootProject.file("HEADER"))
}

subprojects {
    plugins.apply("java-library")
    plugins.apply("net.kyori.indra.licenser.spotless")

    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }

    indraSpotlessLicenser.apply()
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

indraSpotlessLicenser.apply()