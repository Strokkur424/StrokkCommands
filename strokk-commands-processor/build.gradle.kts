plugins {
  alias(libs.plugins.blossom)
}

dependencies {
  implementation(project(":strokk-commands-annotations"))
  compileOnly(libs.bundles.annotations)
}

sourceSets.main {
  blossom.javaSources {
    property("version", project.version.toString())
  }
}