plugins {
  id("commands-publish")
}

java {
  withSourcesJar()
  withJavadocJar()
}

tasks.withType<Javadoc> {
  javadocTool.set(javaToolchains.javadocToolFor {
    this.languageVersion = JavaLanguageVersion.of(25)
  })
  options.encoding = Charsets.UTF_8.name()
}
