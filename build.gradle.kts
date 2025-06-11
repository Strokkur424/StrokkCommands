import com.diffplug.gradle.spotless.SpotlessPlugin
import de.chojo.PublishData

plugins {
    id("java-library")
    id("maven-publish")
    id("de.chojo.publishdata") version "1.4.0"
    id("com.diffplug.spotless") version "7.0.2"
}

group = "net.strokkur"
version = "1.2.3"

allprojects {
    apply {
        plugin<SpotlessPlugin>()
        plugin<PublishData>()
    }
    
    spotless {
        java {
            licenseHeaderFile(rootProject.file("HEADER"))
            target("**/*.java")
        }
    }

    publishData {
        useEldoNexusRepos(true)
        publishComponent("java")
    }
}

subprojects {
    apply {
        plugin<JavaLibraryPlugin>()
    }

    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }

    version = rootProject.version
    group = rootProject.group

    if (name.contains("processor") || name.contains("annotations")) {
        apply {
            plugin<PublishData>()
            plugin<MavenPublishPlugin>()
        }

        publishing {
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
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}
