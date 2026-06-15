plugins {
    id("net.fabricmc.fabric-loom-remap") version "1.14-SNAPSHOT"
}

/**
 * Per-version coordinates, keyed by the Stonecutter node version (the subproject name).
 * `range` is the release label used in the jar name (the version range the jar covers);
 * `mcCompat` is the inclusive Minecraft range the jar declares in fabric.mod.json.
 */
data class VersionDeps(
    val range: String,
    val yarn: String,
    val loader: String,
    val fabricApi: String,
    val mcCompat: String,
)

val perVersion = mapOf(
    "1.19.2"  to VersionDeps("1.19.2",        "1.19.2+build.28", "0.15.11", "0.77.0+1.19.2",  "~1.19.2"),
    "1.19.4"  to VersionDeps("1.19.4",        "1.19.4+build.2",  "0.15.11", "0.87.2+1.19.4",  "~1.19.4"),
    "1.20.4"  to VersionDeps("1.20-1.20.4",   "1.20.4+build.3",  "0.15.3",  "0.93.1+1.20.4",  ">=1.20 <=1.20.4"),
    "1.20.6"  to VersionDeps("1.20.5-1.20.6", "1.20.6+build.1",  "0.15.11", "0.97.8+1.20.6",  ">=1.20.5 <=1.20.6"),
    "1.21.1"  to VersionDeps("1.21-1.21.1",   "1.21.1+build.3",  "0.16.3",  "0.103.0+1.21.1", ">=1.21 <=1.21.1"),
    "1.21.3"  to VersionDeps("1.21.2-1.21.3", "1.21.3+build.2",  "0.16.8",  "0.106.1+1.21.3", ">=1.21.2 <=1.21.3"),
    "1.21.4"  to VersionDeps("1.21.4",        "1.21.4+build.4",  "0.16.9",  "0.114.0+1.21.4", "~1.21.4"),
    "1.21.5"  to VersionDeps("1.21.5",        "1.21.5+build.1",  "0.16.13", "0.119.9+1.21.5", "~1.21.5"),
    "1.21.6"  to VersionDeps("1.21.6-1.21.8", "1.21.6+build.1",  "0.16.14", "0.128.0+1.21.6", ">=1.21.6 <=1.21.8"),
    "1.21.9"  to VersionDeps("1.21.9-1.21.10","1.21.9+build.1",  "0.17.2",  "0.134.0+1.21.9", ">=1.21.9 <=1.21.10"),
    "1.21.11" to VersionDeps("1.21.11",       "1.21.11+build.3", "0.18.4",  "0.140.2+1.21.11", "~1.21.11"),
)

// The Stonecutter node's subproject name is its Minecraft version (e.g. ":1.21.11").
val mcVersion: String = project.name
val deps: VersionDeps = perVersion[mcVersion]
    ?: throw GradleException("No dependency coordinates for Minecraft $mcVersion in build.gradle.kts")

// Kept as plain SemVer for fabric.mod.json; the jar file name adds the range + "v" below.
version = property("mod.version") as String
group = property("mod.group") as String

base {
    // e.g. vanillapings-1.20-1.20.4
    archivesName.set("${property("mod.id")}-${deps.range}")
}

// Final jar name: vanillapings-<range>-v<modVersion>.jar
val jarVersion = "v${property("mod.version")}"
tasks.withType<org.gradle.api.tasks.bundling.AbstractArchiveTask>().configureEach {
    archiveVersion.set(jarVersion)
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings("net.fabricmc:yarn:${deps.yarn}:v2")
    modImplementation("net.fabricmc:fabric-loader:${deps.loader}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${deps.fabricApi}")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
    options.encoding = "UTF-8"
}

tasks.withType<org.gradle.language.jvm.tasks.ProcessResources>().configureEach {
    inputs.property("version", project.version)
    inputs.property("minecraft", deps.mcCompat)
    filesMatching("fabric.mod.json") {
        expand(
            "version" to project.version,
            "minecraft" to deps.mcCompat
        )
    }
}

// Builds this version and copies its remapped jar into the root build/libs folder,
// so a chiseled run collects every version's jar in one place for release.
tasks.register<Copy>("buildAndCollect") {
    group = "build"
    description = "Build this version and copy its jar into the root build/libs."
    from(tasks.named("remapJar"))
    into(rootProject.layout.buildDirectory.dir("libs"))
    dependsOn("build")
}
