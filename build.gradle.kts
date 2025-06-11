plugins {
    id("java-library")
}

group = "net.strokkur"
version = "1.2.0"

subprojects {
    plugins.apply("java-library")
    
    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}