plugins {
  id("commands-publish-sources")
}

dependencies {
  compileOnly(libs.brigadier)
  compileOnlyApi(libs.bundles.annotations)
}
