plugins {
  id("commands-publish")
}

dependencies {
  api(project(":annotations-paper"))
  api(project(":processor-common"))
}
