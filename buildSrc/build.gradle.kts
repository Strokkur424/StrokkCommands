plugins {
  `kotlin-dsl`
}

repositories {
  gradlePluginPortal()
  mavenLocal()
}

dependencies {
  implementation("com.diffplug.spotless:spotless-plugin-gradle:8.0.0")
}

kotlin {
  jvmToolchain(21)
}
