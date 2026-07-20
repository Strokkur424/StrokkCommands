plugins {
  alias(libs.plugins.blossom)
  id("commands-publish")
}

dependencies {
  compileOnlyApi(project(":annotations-common"))
  api(libs.source.map)
  api(libs.auto.service)

  testImplementation(platform(libs.junit.bom))
  testImplementation(libs.junit.jupiter)
  testRuntimeOnly(libs.junit.platform)
}

tasks {
  test {
    useJUnitPlatform()
    testLogging {
      events("skipped", "failed")
    }
  }
}

sourceSets.main {
  blossom.javaSources {
    property("version", project.version.toString())
  }
}
