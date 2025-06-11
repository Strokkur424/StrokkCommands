pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://eldonexus.de/repository/maven-public/")
    }
}

rootProject.name = "StrokkCommands"

include(":strokk-commands-annotations")
include(":strokk-commands-processor")
include(":strokk-commands-test-plugin")
