pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://eldonexus.de/repository/maven-public/")
    }
}

rootProject.name = "StrokkCommands"

include(":strokk-commands")
include(":strokk-commands-test-plugin")
