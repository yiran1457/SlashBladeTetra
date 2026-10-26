plugins {
    id("dev.kikugie.stonecutter")
    id("net.neoforged.moddev.legacyforge") version "2.0.141" apply false
}

stonecutter.active("6.17.0")

// Collects every node's reobfuscated jar into build/libs/<mod version>/ so both nodes
// produce distinct file names in one folder.
tasks.register<Copy>("buildAndCollect") {
    group = "build"
    description = "Builds every node and collects the reobfuscated jars."
    val jars = stonecutter.tasks.named<Jar>("reobfJar")
    dependsOn(jars)
    // A MapProperty is not a File notation: resolve each node's reobfJar archive file instead.
    from(jars.map { nodeTasks -> nodeTasks.values.map { it.flatMap { task -> task.archiveFile } } })
    val base = rootProject.findProperty("mod_version_base").toString()
    val suffix = rootProject.findProperty("mod_version_suffix").toString()
    into(rootProject.layout.buildDirectory.dir("libs/$base-$suffix"))
}
