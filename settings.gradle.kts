pluginManagement {
  repositories {
    gradlePluginPortal()
    maven("https://maven.fabricmc.net/")
  }
}

rootProject.name = "StrokkCommands"

sequenceOf("common", "paper", "velocity").forEach {
  include("processor-$it")
  include("annotations-$it")
}

rootDir.resolve("processors-modded").listFiles()?.forEach {
  include("processor-modded-${it.name}")
  project(":processor-modded-${it.name}").projectDir = it
}

include("annotations-modded")

sequenceOf("paper", "velocity").forEach {
  include("test-plugin-$it")
}

sequenceOf("fabric").forEach {
  include("test-mod-$it")
}
