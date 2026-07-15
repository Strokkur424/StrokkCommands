plugins {
  `kotlin-dsl`
}

repositories {
  mavenCentral()
  gradlePluginPortal()
  mavenLocal()
}

dependencies {
  implementation("com.diffplug.spotless:spotless-plugin-gradle:8.0.0")
  implementation("org.ow2.asm:asm:9.10.1")
  implementation("com.google.code.gson:gson:2.14.0")
}

kotlin {
  jvmToolchain(21)
}
