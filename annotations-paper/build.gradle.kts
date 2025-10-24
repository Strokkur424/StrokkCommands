val commonAnnotations = project(":commands-annotations-common");
val commonAnnotationsSource = commonAnnotations.sourceSets.main.get()

dependencies {
  api(commonAnnotations)
  compileOnly(libs.paper.api)
}

tasks {
  build {
    dependsOn(commonAnnotations.tasks.build)
  }

  jar {
    from(commonAnnotationsSource.output)
  }

  sourcesJar {
    from(commonAnnotationsSource.allSource)
  }

  javadocJar {
    from(commonAnnotationsSource.allSource)
  }
}
