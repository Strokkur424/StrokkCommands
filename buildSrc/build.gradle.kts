plugins {
  `kotlin-dsl`
}

repositories {
  gradlePluginPortal()
  maven("https://central.sonatype.com/repository/maven-snapshots/")
  maven("https://eldonexus.de/repository/maven-releases/")
}

dependencies {
  implementation("com.diffplug.spotless:spotless-plugin-gradle:8.0.0")
  implementation("dev.denwav.hypo:hypo-asm:3.1.0-SNAPSHOT")
  implementation("dev.denwav.hypo:hypo-asm-hydrate:3.1.0-SNAPSHOT")
  implementation("net.strokkur.japutil:code-gen:0.1.0")
}

kotlin {
  jvmToolchain(21)
}
