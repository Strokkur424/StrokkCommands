import com.diffplug.gradle.spotless.SpotlessPlugin

plugins {
  id("java-library")
  id("maven-publish")
  alias(libs.plugins.spotless)
  alias(libs.plugins.blossom) apply false
}

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
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://libraries.minecraft.net")
  }

  java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
  }

  if ((name.contains("processor") || name.contains("annotations")) && !name.contains("-common")) {
    apply {
      plugin<MavenPublishPlugin>()
    }

    if (name.contains("annotations")) {
      tasks.withType<Javadoc> {
        javadocTool.set(javaToolchains.javadocToolFor {
          this.languageVersion = JavaLanguageVersion.of(25)
        })
      }
    }

    java {
      if (name.contains("annotations")) {
        withSourcesJar()
        withJavadocJar()
      }
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
