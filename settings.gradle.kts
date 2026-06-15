pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.5"
    // Applies the correct fabric-loom variant per Minecraft version (so one project spans
    // 1.19.2 through 26.x). Provides the `sc` and `loomx` build accessors.
    id("dev.kikugie.loom-back-compat") version "0.3"
    // Auto-provisions the JDK toolchains (17/21/25) the per-version builds require.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

stonecutter {
    create(rootProject) {
        // One node per API-break boundary; each jar declares an inclusive Minecraft range.
        versions(
            "1.19.2", "1.19.4",
            "1.20.4", "1.20.6",
            "1.21.1", "1.21.3", "1.21.4", "1.21.5", "1.21.6", "1.21.9", "1.21.11",
            "26.1.2"
        )
        vcsVersion = "1.21.11"
    }
}

rootProject.name = "vanillapings"
