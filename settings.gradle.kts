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

include("annotations-common-permission")

sequenceOf("common", "paper", "velocity").forEach {
  include("processor-$it")
  include("annotations-$it")
}

if (System.getenv("SKIP_TESTS") == null) {
  sequenceOf("paper", "velocity").forEach {
    include("test-plugin-$it")
  }
}
