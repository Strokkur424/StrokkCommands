plugins {
  alias(libs.plugins.blossom)
  id("jacoco")
  id("commands-publish")
}

dependencies {
  compileOnlyApi(project(":annotations-common"))

  testImplementation(platform(libs.junit.bom))
  testImplementation(libs.junit.jupiter)
  testRuntimeOnly(libs.junit.platform)
}

jacoco {
  toolVersion = "0.8.14"
}

tasks {
  test {
    useJUnitPlatform()
    testLogging {
      events("skipped", "failed")
    }
  }

  jacocoTestReport {
    dependsOn(test)
    reports {
      xml.required = true
      html.required = true
    }
  }
}

sourceSets.main {
  blossom.javaSources {
    property("version", project.version.toString())
  }
}
