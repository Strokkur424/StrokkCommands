plugins {
  id("commands-common")
  alias(libs.plugins.run.velocity)
}

dependencies {
  compileOnly(libs.velocity.api)
  annotationProcessor(libs.velocity.api)

  compileOnly(project(":annotations-velocity"))
  annotationProcessor(project(":processor-velocity"))
}

tasks.runVelocity {
  velocityVersion(libs.versions.velocity.get())
  jvmArgs("-Xmx2G", "-Xms2G")
}
