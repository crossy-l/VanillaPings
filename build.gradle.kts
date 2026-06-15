plugins {
    // Selects the correct fabric-loom variant for the active Minecraft version.
    id("dev.kikugie.loom-back-compat")
}

val modVersion = property("mod.version") as String
version = modVersion
group = property("mod.group") as String

// Per-node values from stonecutter.properties.toml (typed so the generic get<T> resolves).
val modRange: String = sc.properties["mod.range"]
val fabricApiVersion: String = sc.properties["deps.fabric_api"]
val mcCompat: String = sc.properties["mod.mc_compat"]

// Minecraft's Java baseline per version (26.x bumped to 25; 1.20.5+ is 21; 1.18+ is 17).
val requiredJava: JavaVersion = when {
    sc.current.parsed >= "26.1"   -> JavaVersion.VERSION_25
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    else                          -> JavaVersion.VERSION_17
}

base {
    // e.g. vanillapings-1.20-1.20.4
    archivesName.set("${property("mod.id")}-$modRange")
}

// Final jar name: vanillapings-<range>-v<modVersion>.jar
tasks.withType<org.gradle.api.tasks.bundling.AbstractArchiveTask>().configureEach {
    archiveVersion.set("v$modVersion")
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    // Official Mojang mappings (Yarn ends at 1.21.11; Mojmap covers every version incl. 26.x).
    loomx.applyMojangMappings()
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
}

java {
    withSourcesJar()
    sourceCompatibility = requiredJava
    targetCompatibility = requiredJava
    toolchain {
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.withType<org.gradle.language.jvm.tasks.ProcessResources>().configureEach {
    val v = project.version.toString()
    val mc = mcCompat
    inputs.property("version", v)
    inputs.property("minecraft", mc)
    filesMatching("fabric.mod.json") {
        expand("version" to v, "minecraft" to mc)
    }
}

// Build this version and copy its jar into the root build/libs for release collection.
tasks.register<Copy>("buildAndCollect") {
    group = "build"
    description = "Build this version and copy its jar into the root build/libs."
    from(loomx.modJar.map { it.archiveFile })
    into(rootProject.layout.buildDirectory.dir("libs"))
    dependsOn("build")
}
