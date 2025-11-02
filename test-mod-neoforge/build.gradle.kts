plugins {
  id("commands-common")
  alias(libs.plugins.neoforge.moddev)
}

dependencies {
  compileOnly(project(":annotations-modded"))
  annotationProcessor(project(":processor-neoforge"))
}

neoForge {
  version = libs.versions.neoforge.version.get()

  runs {
    create("client") {
      client()
    }

    create("server") {
      server()
      programArgument("--nogui")
    }
  }

  mods {
    create("testmod") {
      sourceSet(sourceSets["main"])
    }
  }
}
