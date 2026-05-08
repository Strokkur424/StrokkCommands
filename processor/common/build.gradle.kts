plugins {
  alias(libs.plugins.blossom)
  id("commands-publish")
}

dependencies {
  compileOnlyApi(project(":annotations-common"))

  testImplementation(platform(libs.junit.bom))
  testImplementation(libs.junit.jupiter)
  testRuntimeOnly(libs.junit.platform)
}

tasks.test {
  useJUnitPlatform()
  testLogging {
    events("passed", "skipped", "failed")
  }
}

sourceSets.main {
  blossom.javaSources {
    property("version", project.version.toString())
  }
}
