plugins {
  id("commands-publish-sources")
}

dependencies {
  api(project(":annotations-common"))
  api(project(":annotations-common-permission"))
  compileOnly(libs.velocity.api)
  compileOnly(libs.brigadier)
}
