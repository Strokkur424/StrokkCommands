plugins {
  id("commands-common")
  alias(libs.plugins.run.paper)
  alias(libs.plugins.shadow)
}

dependencies {
  compileOnly(libs.paper.api)

  compileOnly(project(":annotations-paper"))
  annotationProcessor(project(":processor-paper"))

  implementation(libs.guice)
}

tasks.runServer {
  minecraftVersion(libs.versions.minecraft.get())
  jvmArgs("-Xmx2G", "-Xms2G", "-Dcom.mojang.eula.agree=true")
}
