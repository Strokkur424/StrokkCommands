plugins {
  alias(libs.plugins.fabric.loom)
}

loom {
  splitEnvironmentSourceSets()
  mods.create("testmod") {
    sourceSet(sourceSets["main"])
    sourceSet(sourceSets["client"])
  }
}

dependencies {
  minecraft(libs.minecraft)
  mappings(loom.officialMojangMappings())
  modImplementation(libs.fabric.loader)
  modImplementation(libs.fabric.api)

  compileOnly(project(":annotations-modded"))
  annotationProcessor(project(":processor-fabric"))
}
