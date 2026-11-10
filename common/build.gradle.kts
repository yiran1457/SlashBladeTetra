plugins {
    id("net.neoforged.moddev.legacyforge")
}

val p = { name: String -> rootProject.property(name).toString() }
val modId = p("mod_id")
val mainSourceSet = sourceSets.getByName("main")

legacyForge {
    version = p("minecraft_version") + "-" + p("forge_version")
    parchment {
        mappingsVersion.set(p("parchment_mappings_version"))
        minecraftVersion.set(p("parchment_minecraft_version"))
    }
    // MDG auto-detection scans resources srcDirs; in a multi-module build that can point at
    // another module's output and create an undeclared task dependency.
    setAccessTransformers(rootProject.file("common/src/main/resources/META-INF/accesstransformer.cfg"))
    ideSyncTask(tasks.named("jar"))
}

// The mixin config is registered by the version modules: they own the mod that actually runs.
// Refmap generation still has to happen here, because the mixin classes live in :common.
mixin {
    add(mainSourceSet, "$modId.refmap.json")
    config("$modId.mixins.json")
}

// common carries the whole mod except the version-specific seam. Tetra/mutil are compile-only
// here: each version module supplies the real pair, so common cannot pick one by accident.
dependencies {
    compileOnly("org.jetbrains:annotations:24.0.1")
    modImplementation("curse.maven:cialloblade-1281291:8067707")

    modImplementation("curse.maven:jei-238222:9298209")
    modCompileOnly("curse.maven:extraholopage-1301292:7021781")

    modImplementation("curse.maven:slashblade-resharped-1022428:8090912")
    modImplementation("curse.maven:mysterious-mountain-lib-368098:6955193")
    modImplementation("curse.maven:slashblade-hf-blade-1299888:6917439")
    modImplementation("curse.maven:the-last-smith-306385:6982449")

    annotationProcessor("org.spongepowered:mixin:0.8.7:processor")

    // API used to compile the shared code. The 6.9.0 subset is the narrower one, so code that
    // compiles here also compiles against any later Tetra.
    modCompileOnly("curse.maven:tetra-289712:" + p("tetra_api_file_id"))
    modCompileOnly("curse.maven:mutil-351914:" + p("mutil_api_file_id"))
}

// FML reads this manifest key to find the mixin config. Without it the config is never loaded and
// every mixin in this mod silently does nothing (symptom: ClassCastException where a mixin was
// supposed to make a Tetra class implement a local interface).
tasks.named<Jar>("jar") {
    manifest.attributes(mapOf("MixinConfigs" to (modId + ".mixins.json")))
}
