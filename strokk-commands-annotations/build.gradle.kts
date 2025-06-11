import java.util.Properties

plugins {
    id("de.chojo.publishdata") version "1.4.0"
    id("maven-publish")
}

val credentialsProperties: Properties = Properties()
val credentialsFile = rootDir.resolve("MavenCredentials.properties")

if (credentialsFile.exists()) {
    val reader = credentialsFile.reader()
    credentialsProperties.load(reader)
}

group = rootProject.group
version = rootProject.version

dependencies {
    compileOnly("org.jspecify", "jspecify", "1.0.0")
}

publishData {
    useEldoNexusRepos(true)
    publishComponent("java")
}

publishing {
    publications.create<MavenPublication>("maven") {
        publishData.configurePublication(this)
    }

    repositories {
        maven {
            authentication {
                credentials(PasswordCredentials::class) {
                    username = credentialsProperties.getProperty("NEXUS_USERNAME")
                    password = credentialsProperties.getProperty("NEXUS_PASSWORD")
                }
            }
            
            name = "EldoNexus"
            setUrl(publishData.getRepository())
        }
    }
}