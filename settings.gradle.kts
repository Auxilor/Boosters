pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenLocal()
        maven("https://repo.jpenilla.xyz/snapshots/")
        maven("https://jitpack.io")
    }
}

rootProject.name = "Boosters"

// Core
include(":eco-core")
include(":eco-core:core-plugin")
