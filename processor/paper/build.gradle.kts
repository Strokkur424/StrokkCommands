import net.strokkur.build.GeneratePaperArgumentConverterDefinitions

plugins {
  id("commands-publish")
}

dependencies {
  api(project(":annotations-paper"))
  api(project(":processor-common"))
  annotationProcessor(libs.auto.service)
}

val parsePaperJar = tasks.register<GeneratePaperArgumentConverterDefinitions>("parsePaperJar") {
  description = "Downloads and extracts command argument type information from an API JAR."

  val ver = "26.2.build.60-beta"
  url = "https://artifactory.papermc.io/artifactory/releases/io/papermc/paper/paper-api/${ver}/paper-api-${ver}.jar"
  targetDir = layout.buildDirectory.dir("paper-gen")
  target = "net.strokkur.commands.internal.paper.PaperBrigadierArgumentConverter"
}

sourceSets.main {
  java.srcDir(parsePaperJar.flatMap { it.targetDir.dir("generated") })
}

tasks.compileJava {
  dependsOn(parsePaperJar)
}
