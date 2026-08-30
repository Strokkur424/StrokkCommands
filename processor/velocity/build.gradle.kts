plugins {
  id("commands-publish")
}

dependencies {
  api(project(":annotations-velocity"))
  api(project(":processor-common"))
  annotationProcessor(libs.auto.service)
}
