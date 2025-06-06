plugins {
    id("java-library")
}

group = "net.strokkur"
version = "1.0-SNAPSHOT"

subprojects {
    plugins.apply("java-library")
    
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}