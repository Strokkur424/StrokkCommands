pluginManagement {
  repositories {
    gradlePluginPortal()
    maven("https://eldonexus.de/repository/maven-public/")
  }
}

rootProject.name = "StrokkCommands"

sequenceOf("common", "paper").forEach {
  include("commands-processor-$it")
  include("commands-annotations-$it")
  project(":commands-processor-$it").projectDir = file("processor-$it")
  project(":commands-annotations-$it").projectDir = file("annotations-$it")
}
include(":test-plugin")
