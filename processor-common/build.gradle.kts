plugins {
  alias(libs.plugins.blossom)
  id("commands-publish")
}

dependencies {
  compileOnlyApi(project(":annotations-common"))
}

sourceSets.main {
  blossom.javaSources {
    property("version", project.version.toString())
  }
}
