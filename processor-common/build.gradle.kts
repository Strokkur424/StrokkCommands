plugins {
  alias(libs.plugins.blossom)
}

dependencies {
  compileOnlyApi(project(":annotations-common"))
}

sourceSets.main {
  blossom.javaSources {
    property("version", project.version.toString())
  }
}
