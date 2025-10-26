import com.diffplug.gradle.spotless.SpotlessPlugin

plugins {
  id("java-library")
  id("maven-publish")
  alias(libs.plugins.spotless)
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
    maven("https://eldonexus.de/repository/maven-releases/")
  }

  java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
  }

  if ((name.contains("processor") || name.contains("annotations"))) {
    apply {
      plugin<MavenPublishPlugin>()
    }

    if (name.contains("annotations")) {
      java {
        withSourcesJar()
        withJavadocJar()
      }

      tasks.withType<Javadoc> {
        javadocTool.set(javaToolchains.javadocToolFor {
          this.languageVersion = JavaLanguageVersion.of(25)
        })
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
          if (version.toString().endsWith("-SNAPSHOT")) {
            setUrl("https://eldonexus.de/repository/maven-snapshots/")
          } else {
            setUrl("https://eldonexus.de/repository/maven-releases/")
          }
        }
      }

      publications.create<MavenPublication>("maven") {
        from(components["java"])
        withBuildIdentifier()
      }
    }

    tasks.withType<GenerateModuleMetadata> {
      enabled = true
    }
  }
}
