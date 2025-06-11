plugins {
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

dependencies {
    compileOnly(project(":strokk-commands-annotations"))
    annotationProcessor(project(":strokk-commands-processor"))
    
    compileOnly("io.papermc.paper:paper-api:1.21.5-R0.1-SNAPSHOT")
}

tasks.runServer {
    minecraftVersion("1.21.5")
    jvmArgs("-Xmx2G", "-Xms2G", "-Dcom.mojang.eula.agree=true")
}

tasks.processResources {
    filesMatching("paper-plugin.yml") {
        expand("version" to version)
    }
}