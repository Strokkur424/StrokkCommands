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

include("annotations-modded")
sequenceOf("fabric").forEach {
  include("processor-$it")
}

if (System.getenv("SKIP_TESTS") == null) {
  sequenceOf("paper", "velocity").forEach {
    include("test-plugin-$it")
  }

  sequenceOf("fabric").forEach {
    include("test-mod-$it")
  }
}
