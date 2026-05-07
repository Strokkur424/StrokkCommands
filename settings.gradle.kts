pluginManagement {
  repositories {
    gradlePluginPortal()
    maven("https://maven.fabricmc.net/")
  }
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "StrokkCommands"

fun importProjectsIn(folder: File) {
  val name = folder.name;
  rootDir.resolve(name).listFiles { it.isDirectory }?.forEach {
    include(":${name}-${it.name}")
    project(":${name}-${it.name}").projectDir = it
  }
}

importProjectsIn(rootDir.resolve("annotations"))
importProjectsIn(rootDir.resolve("processor"))

if (System.getenv("SKIP_TESTS") == null) {
  importProjectsIn(rootDir.resolve("test-plugin"))
}
