import net.strokkur.build.ParsePaperApiJarTask

plugins {
  id("commands-publish")
}

dependencies {
  api(project(":annotations-paper"))
  api(project(":processor-common"))
  annotationProcessor(libs.auto.service)
}

val parsePaperJar = tasks.register<ParsePaperApiJarTask>("parsePaperJar") {
  description = "Downloads and extracts command argument atype information from an API JAR."

  val ver = "26.2.build.60-beta"
  url = "https://artifactory.papermc.io/artifactory/releases/io/papermc/paper/paper-api/${ver}/paper-api-${ver}.jar"
  apiJar = layout.buildDirectory.file("external-data/api.jar")

  outputs.upToDateWhen { false }
}

tasks.compileJava {
  dependsOn(parsePaperJar)
}
