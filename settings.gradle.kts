pluginManagement {
  repositories {
    gradlePluginPortal()
    maven("https://eldonexus.de/repository/maven-public/")
  }
}

rootProject.name = "StrokkCommands"

sequenceOf("common", "paper", "velocity").forEach {
  include("processor-$it")
  include("annotations-$it")
}

sequenceOf("paper", "velocity").forEach {
  include(":test-plugin-$it")
}
