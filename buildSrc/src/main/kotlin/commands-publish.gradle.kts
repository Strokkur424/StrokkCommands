plugins {
  id("maven-publish")
  id("commands-common")
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
