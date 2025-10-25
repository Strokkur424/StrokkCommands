plugins {
  alias(libs.plugins.run.velocity)
}

dependencies {
  compileOnly(libs.velocity.api)
  annotationProcessor(libs.velocity.api)

  compileOnly(project(":commands-annotations-velocity"))
  annotationProcessor(project(":commands-processor-velocity"))
}

tasks.runVelocity {
  velocityVersion(libs.versions.velocity.get())
  jvmArgs("-Xmx2G", "-Xms2G")
}
