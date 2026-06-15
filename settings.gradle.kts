pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
        maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.5"
}

stonecutter {
    create(rootProject) {
        // One node per API-break boundary; each jar declares an inclusive Minecraft range
        // covering the point releases in between.
        versions(
            "1.19.2", "1.19.4",
            "1.20.4", "1.20.6",
            "1.21.1", "1.21.3", "1.21.4", "1.21.5", "1.21.6", "1.21.9", "1.21.11"
        )
        vcsVersion = "1.21.11"
    }
}

rootProject.name = "vanillapings"
