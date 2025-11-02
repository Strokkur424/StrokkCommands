plugins {
  id("java-library")
  id("com.diffplug.spotless")
}

spotless {
  java {
    licenseHeaderFile(rootProject.file("HEADER"))
    target("**/*.java")
  }
}

repositories {
  mavenCentral()
  maven("https://repo.papermc.io/repository/maven-public/")
  maven("https://eldonexus.de/repository/maven-releases/")
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.withType<JavaCompile>().configureEach {
  options.release = 21
}
