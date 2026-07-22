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
  implementation("dev.denwav.hypo:hypo-asm:3.0.0")
  implementation("dev.denwav.hypo:hypo-asm-hydrate:3.0.0")
  implementation("net.strokkur.japutil:code-gen:0.1.0-SNAPSHOT")
}

kotlin {
  jvmToolchain(21)
}
