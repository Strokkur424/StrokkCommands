plugins {
  id("commands-common")
}

dependencies {
  implementation(libs.source.map)
  implementation(libs.gson)
  compileOnly(libs.bundles.annotations)
}
