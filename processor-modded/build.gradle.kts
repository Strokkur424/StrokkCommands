plugins {
  id("commands-publish")
}

dependencies {
  api(project(":annotations-modded"))
  api(project(":processor-common"))
}
