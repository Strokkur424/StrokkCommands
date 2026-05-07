plugins {
  id("java-library")
  id("checkstyle")
  id("com.diffplug.spotless")
}

spotless {
  java {
    licenseHeaderFile(rootProject.file("HEADER"))
    target("**/*.java")
  }
}

checkstyle {
  toolVersion = "13.4.2"
  configDirectory = rootDir.resolve(".checkstyle")
}

repositories {
  mavenCentral()
  maven("https://repo.papermc.io/repository/maven-public/")
  maven("https://eldonexus.de/repository/maven-releases/")
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
  withType<JavaCompile>().configureEach {
    options.release = 21
  }
  withType<Checkstyle>().configureEach {
    minHeapSize = "200M"
    maxHeapSize = "200M"
  }
}
