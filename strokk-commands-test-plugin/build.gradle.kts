plugins {
  alias(libs.plugins.run.paper)
}

dependencies {
  compileOnly(project(":strokk-commands-annotations"))
  annotationProcessor(project(":strokk-commands-processor"))

  compileOnly(libs.paper.api)
}

tasks.runServer {
  minecraftVersion(libs.versions.minecraft.get())
  jvmArgs("-Xmx2G", "-Xms2G", "-Dcom.mojang.eula.agree=true")
}