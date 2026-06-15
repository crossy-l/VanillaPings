plugins {
    id("dev.kikugie.stonecutter")
}

// Active version for the IDE and single-version dev tasks. Per-version differences are
// handled by //? conditionals in the source (see com.vanillapings.compat.Compat).
stonecutter active "1.21.11"

// `gradlew chiseledBuild` builds every version; `chiseledBuildAndCollect` also gathers
// each jar into the root build/libs folder.
tasks.register("chiseledBuild") {
    group = "build"
    description = "Build every Stonecutter version."
    dependsOn(stonecutter.versions.map { ":${it.project}:build" })
}
tasks.register("chiseledBuildAndCollect") {
    group = "build"
    description = "Build every version and collect the jars into build/libs."
    dependsOn(stonecutter.versions.map { ":${it.project}:buildAndCollect" })
}
