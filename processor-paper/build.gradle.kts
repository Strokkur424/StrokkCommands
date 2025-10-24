plugins {
  alias(libs.plugins.blossom)
}

val commonProcessor = project(":commands-processor-common");
val commonProcessorSource = commonProcessor.sourceSets.main.get()

dependencies {
  implementation(project(":commands-annotations-paper"))
  compileOnly(commonProcessor)
  compileOnly(libs.bundles.annotations)
}

tasks {
  build {
    dependsOn(commonProcessor.tasks.build)
  }

  jar {
    from(commonProcessorSource.output)
  }
}
