plugins {
  alias(libs.plugins.run.paper)
}

dependencies {
  compileOnly(libs.paper.api)

  compileOnly(project(":commands-annotations-paper"))
  annotationProcessor(project(":commands-processor-paper"))
}

tasks.runServer {
  minecraftVersion(libs.versions.minecraft.get())
  jvmArgs("-Xmx2G", "-Xms2G", "-Dcom.mojang.eula.agree=true")
}
