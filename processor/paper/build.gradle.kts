plugins {
  id("commands-publish")
}

dependencies {
  api(project(":annotations-paper"))
  api(project(":processor-common"))
  annotationProcessor(project(":internal-paper-ap"))
  compileOnly(libs.paper.api)
}
