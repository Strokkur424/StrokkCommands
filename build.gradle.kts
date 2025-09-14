import com.diffplug.gradle.spotless.SpotlessPlugin
import de.chojo.PublishData

plugins {
  id("java-library")
  id("maven-publish")
  alias(libs.plugins.publishdata)
  alias(libs.plugins.spotless)
}

group = "net.strokkur"
version = "1.3.0"

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
