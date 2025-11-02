pluginManagement {
  repositories {
    gradlePluginPortal()
    maven("https://maven.fabricmc.net/")
  }
}

rootProject.name = "StrokkCommands"

include("annotations-common-permission")

sequenceOf("common", "paper", "velocity", "modded").forEach {
  include("processor-$it")
  include("annotations-$it")
}

sequenceOf("fabric", "neoforge").forEach {
  include("processor-$it")
}

if (System.getenv("SKIP_TESTS") == null) {
  sequenceOf("paper", "velocity").forEach {
    include("test-plugin-$it")
  }

  sequenceOf("fabric", "neoforge").forEach {
    include("test-mod-$it")
  }
}
