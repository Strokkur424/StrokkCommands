import com.diffplug.gradle.spotless.SpotlessPlugin

plugins {
  id("java-library")
  id("maven-publish")
  alias(libs.plugins.spotless)
  alias(libs.plugins.blossom) apply false
}

group = "net.strokkur"
version = "1.4.1"

allprojects {
  apply {
    plugin<SpotlessPlugin>()
  }

  spotless {
    java {
      licenseHeaderFile(rootProject.file("HEADER"))
      target("**/*.java")
    }
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
          setUrl("https://eldonexus.de/repository/maven-releases/")
        }
      }

      publications.create<MavenPublication>("maven") {
        from(components["java"])

        version = project.version.toString()
        artifactId = project.name
        groupId = project.group.toString()

        versionMapping {
          usage("java-api") {
            fromResolutionOf("runtimeClasspath")
          }
          usage("java-runtime") {
            fromResolutionResult()
          }
        }

        withBuildIdentifier()
      }
    }

    tasks.withType<GenerateModuleMetadata> {
      enabled = true
    }
  }
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}
