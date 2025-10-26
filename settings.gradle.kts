pluginManagement {
  repositories {
    gradlePluginPortal()
    maven("https://eldonexus.de/repository/maven-public/")
  }
}

rootProject.name = "StrokkCommands"

sequenceOf("commands").forEach {
  include("$it-processor")
  include("$it-annotations")
  project(":$it-processor").projectDir = file("strokk-$it-processor")
  project(":$it-annotations").projectDir = file("strokk-$it-annotations")
}
if (System.getenv("SKIP_TESTS") == null) {
  include(":test-plugin")
}
