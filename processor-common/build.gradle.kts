plugins {
  alias(libs.plugins.blossom)
}

dependencies {
  compileOnlyApi(project(":commands-annotations-common"))
}

sourceSets.main {
  blossom.javaSources {
    property("version", project.version.toString())
  }
}
