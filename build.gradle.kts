import me.modmuss50.mpp.ModPublishExtension
import me.modmuss50.mpp.ReleaseType
import net.neoforged.moddevgradle.dsl.ModDevExtension
import net.neoforged.moddevgradle.legacyforge.dsl.LegacyForgeExtension
import net.neoforged.moddevgradle.legacyforge.dsl.MixinExtension
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.language.jvm.tasks.ProcessResources
import org.slf4j.event.Level

plugins {
    id("idea")
    // Declared once here so the subprojects can apply it without repeating the version.
    id("net.neoforged.moddev.legacyforge") version "2.0.141" apply false
    id("me.modmuss50.mod-publish-plugin") version "2.2.1" apply false
}

group = property("mod_group_id").toString()

// property() returns Any, so every property read has to be stringified once here. Declared as a
// Project extension so a version module reads its OWN gradle.properties, not the root project's.
fun Project.prop(name: String): String = property(name).toString()

// A version module is a module with its own gradle.properties declaring tetra_version. Everything
// that differs between Tetra versions therefore lives in data, and the module's build script stays
// at three lines instead of duplicating ~110 lines per version.
fun Project.tetraVersion(): String? = findProperty("tetra_version")?.toString()

fun Project.configureTetraNode() {
    // The main source set of :common is part of this module's mod, so it must be evaluated first.
    // Declaring that makes it explicit instead of relying on the include order in settings.
    evaluationDependsOn(":common")

    // Read once up front so nothing below can accidentally resolve a property against another
    // project's receiver.
    val modId = prop("mod_id")
    val modVersionBase = prop("mod_version_base")
    val tetraVersion = prop("tetra_version")
    val slashbladeVersion = prop("slashblade_version")
    val modName = prop("mod_name")
    // Named mcVer/forgeVer, not minecraftVersion/forgeVersion: those names are properties of the
    // parchment and legacyForge blocks, and a local of the same name shadows them inside the lambda.
    val mcVer = prop("minecraft_version")
    val forgeVer = prop("forge_version")
    val tetraVersionRange = prop("tetra_version_range")
    val mainSourceSet = extensions.getByType<SourceSetContainer>().getByName("main")
    val commonSourceSet = project(":common").extensions.getByType<SourceSetContainer>().getByName("main")

    // <mod base>-<tetra version>-<slashblade version>, matching the published naming scheme.
    version = listOf(modVersionBase, tetraVersion, slashbladeVersion).joinToString("-")

    extensions.configure<BasePluginExtension> {
        archivesName.set(modName)
    }

    // The mixin config is registered by the version modules: they own the mod that actually runs.
    // Refmap generation still has to happen in :common, because the mixin classes live there.
    // Only config() is called here. mixin.add() would hand -AoutRefMapFile/-AmappingTypes to this
    // module's compileJava, which has no mixin annotation processor, and javac reports every one of
    // those options as unrecognised. The refmap reaches the jar through mirrorMixinMetadata below.
    extensions.configure<MixinExtension> {
        config("$modId.mixins.json")
    }

    extensions.configure<LegacyForgeExtension> {
        this.version = "$mcVer-$forgeVer"
        parchment {
            mappingsVersion.set(prop("parchment_mappings_version"))
            minecraftVersion.set(prop("parchment_minecraft_version"))
        }
        // MDG auto-detection scans resources srcDirs; in a multi-module build that can point at
        // another module's output and create an undeclared task dependency.
        setAccessTransformers(rootProject.file("common/src/main/resources/META-INF/accesstransformer.cfg"))
        runs {
            create("client") {
                client()
                systemProperty("forge.enabledGameTestNamespaces", modId)
            }
            create("server") {
                server()
                systemProperty("forge.enabledGameTestNamespaces", modId)
            }
            configureEach {
                systemProperty("forge.logging.markers", "REGISTRIES")
                logLevel.set(Level.INFO)
            }
        }
        mods {
            create(modId) {
                // The mod is split across two modules; both source sets belong to the same mod id,
                // so MDG must see the version module and common together.
                sourceSet(mainSourceSet)
                sourceSet(commonSourceSet)
            }
        }
    }

    // mods.toml is generated per version module: the Tetra version range differs, so it cannot live
    // in common's resources. A missing property is an error, which is what we want here.
    val generateModMetadata = tasks.register<ProcessResources>("generateModMetadata") {
        val replaceProperties = mapOf(
            "minecraft_version" to mcVer,
            "minecraft_version_range" to prop("minecraft_version_range"),
            "forge_version" to forgeVer,
            "forge_version_range" to prop("forge_version_range"),
            "loader_version_range" to prop("loader_version_range"),
            "mod_id" to modId,
            "mod_name" to modName,
            "mod_license" to prop("mod_license"),
            "mod_authors" to prop("mod_authors"),
            "mod_description" to prop("mod_description"),
            "mod_version" to project.version.toString(),
            "tetra_version_range" to tetraVersionRange,
        )
        inputs.properties(replaceProperties)
        expand(replaceProperties)
        from(rootProject.file("common/src/main/templates"))
        into(layout.buildDirectory.dir("generated/sources/modMetadata"))
    }
    mainSourceSet.resources.srcDir(generateModMetadata)

    extensions.configure<ModDevExtension> {
        ideSyncTask(generateModMetadata)
        ideSyncTask(tasks.named("jar"))
    }

    // The mixin annotation processor runs on :common (where the mixin classes live) and writes its
    // metadata to common/build/mixin. reobfJar looks for it under this module's own build/mixin, so
    // the generated files are mirrored here first.
    val mirrorMixinMetadata = tasks.register<Copy>("mirrorMixinMetadata") {
        dependsOn(project(":common").tasks.named("compileJava"))
        from(project(":common").layout.buildDirectory.dir("mixin"))
        into(layout.buildDirectory.dir("mixin"))
    }

    // One jar for players. MDG's reobfJar carries classes only, so common's classes and the mixin
    // metadata are added explicitly.
    //
    // Inputs are TASK PROVIDERS, never bare directories: a raw File path carries no producer
    // information, so Gradle would reject it with "uses this output of task X without declaring an
    // explicit or implicit dependency".
    tasks.withType<Jar>().configureEach {
        dependsOn(mirrorMixinMetadata)
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        // Common's resources (assets, data, mixins.json) do not travel through a project dependency.
        from(project(":common").tasks.named<ProcessResources>("processResources"))
        // builtBy-carrying collection, so this one resolves its producer on its own.
        from(commonSourceSet.output.classesDirs)
        // The mirrored copy, so the dependency edge comes from mirrorMixinMetadata.
        from(mirrorMixinMetadata)
        // FML discovers mixin configs through this manifest key. It must be on the jar MDG uses for
        // the dev run (devlibs) as well as the published one, otherwise every mixin in :common
        // silently does nothing at runtime while the same code works as a shipped jar.
        manifest.attributes(mapOf("MixinConfigs" to "$modId.mixins.json"))
    }

    tasks.named<Jar>("reobfJar") {
        from(mainSourceSet.output.resourcesDir)
    }

    registerChangelogTask()
    configurePublishing(modId, tetraVersion)
}

// Changelog for CurseForge: taken from git, so the notes cannot drift from the actual history.
//
// This must run in a Task, not during configuration: launching git at configuration time is
// rejected by the configuration cache (and would re-run on every build). publishCurseforge depends
// on this task and reads its output file, so git is only touched when publishing.
//
// Scope: commits since the newest one whose subject carries a different version (the previous
// release); falls back to the last 10 commits when no such marker exists. Chinese text comes
// straight from git; the English half is looked up in gradle/changelog-en.properties, and a
// missing entry keeps the original line so an unfinished translation is visible rather than silent.
abstract class GenerateChangelog : DefaultTask() {
    @get:Input
    abstract val currentVersion: Property<String>

    @get:Input
    abstract val repositoryPath: Property<String>

    @get:Optional
    @get:InputFile
    abstract val translationFile: RegularFileProperty

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    // Task.project must not be touched at execution time under the configuration cache, so the
    // repository root is passed in as a plain path. Not @InputDirectory: hashing the whole repo
    // would try to read live files such as .gradle locks and fail.

    private fun git(vararg args: String): String {
        val out = java.io.ByteArrayOutputStream()
        val process = ProcessBuilder(listOf("git") + args.toList())
            .directory(java.io.File(repositoryPath.get()))
            .redirectErrorStream(true)
            .start()
        process.inputStream.copyTo(out)
        process.waitFor()
        return out.toString(Charsets.UTF_8.name()).trim()
    }

    @TaskAction
    fun generate() {
        val version = currentVersion.get()
        val previousRelease = git("log", "--format=%H %s", "-n", "60")
            .lineSequence()
            .mapNotNull { line ->
                val hash = line.substringBefore(' ')
                val subject = line.substringAfter(' ', "")
                val found = Regex("^(\\d+(?:\\.\\d+)+)").find(subject)?.groupValues?.get(1)
                if (found != null && found != version) hash else null
            }
            .firstOrNull()

        val range = if (previousRelease != null) previousRelease + "..HEAD" else "-10"
        val raw = git("log", "--format=%s%x1f%b%x1e", range)
            .split('\u001e')
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val rendered = raw.joinToString("\n") { entry ->
            val subject = entry.substringBefore('\u001f').trim()
            val body = entry.substringAfter('\u001f', "").trim()
            // A commit body is hard-wrapped in git, so a bullet continues on the following lines.
            // Accumulate until the next bullet instead of emitting each physical line.
            val bullets = body.lineSequence()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .fold(mutableListOf<String>()) { acc, line ->
                    if (line.startsWith("- ")) acc.add(line)
                    else if (acc.isNotEmpty()) acc[acc.size - 1] = acc.last() + " " + line
                    acc
                }
                .joinToString("\n") { "  " + it }
            if (bullets.isEmpty()) "- " + subject else "- " + subject + "\n" + bullets
        }.ifEmpty { "- No changes recorded." }

        val table = LinkedHashMap<String, String>()
        translationFile.orNull?.asFile?.takeIf { it.isFile }?.reader(Charsets.UTF_8)?.useLines { lines ->
            lines.forEach { line ->
                val trimmed = line.trim()
                if (trimmed.isEmpty() || trimmed.startsWith("#")) return@forEach
                val key = trimmed.substringBefore('=').trim()
                if (key.isNotEmpty()) table[key] = trimmed.substringAfter('=', "").trim()
            }
        }

        val english = rendered.lineSequence().joinToString("\n") { line ->
            val bullet = line.trim().removePrefix("- ").trim()
            val hit = table.entries.firstOrNull { (zh, _) -> bullet.contains(zh) }?.value
            if (hit != null && line.trim().startsWith("- ")) line.replace(bullet, hit) else line
        }

        outputFile.get().asFile.writeText(
            "### 变更 (" + version + ")\n\n" + rendered +
                "\n\n### Changes (" + version + ")\n\n" + english,
            Charsets.UTF_8
        )
    }
}

fun Project.registerChangelogTask() {
    if (tasks.findByName("generateChangelog") != null) return
    tasks.register<GenerateChangelog>("generateChangelog") {
        currentVersion.set(prop("mod_version_base"))
        repositoryPath.set(rootProject.projectDir.absolutePath)
        translationFile.set(rootProject.layout.projectDirectory.file("gradle/changelog-en.properties"))
        outputFile.set(layout.buildDirectory.file("publishMods/changelog.md"))
    }
}

// Both version modules publish to the SAME CurseForge project (1294523); the Tetra version in the
// file name is what tells the two apart, matching how earlier releases were uploaded there.
//
// The plugin is applied per module rather than once at the root, because each module has its own
// mods.toml (different tetra_version_range) and its own jar, so publishing is a per-module act.
fun Project.configurePublishing(modId: String, nodeVersion: String) {
    apply(plugin = "me.modmuss50.mod-publish-plugin")

    // The CurseForge token lives in ~/.gradle/gradle.properties (or the env for CI) and is never
    // written into a tracked file.
    val cfToken = providers.gradleProperty("curseforge_api_key")
        .orElse(providers.environmentVariable("CURSEFORGE_API_KEY"))

    extensions.configure<ModPublishExtension> {
        // reobfJar, never jar: under ModDevGradle Legacy the plain jar task writes the dev jar to
        // build/devlibs/ and is NOT remapped, so uploading it ships a package that fails for players
        // with NoSuchMethodError while the build still reports success.
        file.set(tasks.named<Jar>("reobfJar").flatMap { it.archiveFile })
        // Changelog comes from git, so the release notes cannot drift from the history. Override
        // with -Ppublish_changelog="..." for a hand-written note.
        val changelogTask = tasks.named<GenerateChangelog>("generateChangelog")
        changelog.set(
            providers.gradleProperty("publish_changelog").orElse(
                changelogTask.flatMap { it.outputFile }.map { it.asFile.readText(Charsets.UTF_8) }
            )
        )
        // The plugin reads the changelog as a plain string, which carries no producer information,
        // so the dependency edge has to be declared explicitly.
        tasks.withType<me.modmuss50.mpp.PublishModTask>().configureEach {
            dependsOn(changelogTask)
        }
        // findProperty, not providers.gradleProperty: the latter does not see -P values in this
        // position, so `-Ppublish_type=beta` would silently be ignored.
        //
        // No default: the Alpha/Beta/Release badge is a release decision, not a build detail, and
        // CurseForge cannot change it after upload. Only the publish task evaluates this, so an
        // ordinary `build` never needs it.
        val declaredType = findProperty("publish_type")?.toString() ?: System.getenv("PUBLISH_TYPE")
        type.set(
            providers.provider<ReleaseType> {
                when (declaredType?.lowercase()) {
                    "alpha" -> ALPHA
                    "beta" -> BETA
                    "stable", "release" -> STABLE
                    null -> throw GradleException(
                        "publish_type is not set. Pick the CurseForge release channel explicitly: " +
                            "put publish_type (stable|beta|alpha) in gradle.properties, or pass " +
                            "-Ppublish_type=<value>. There is no default because CurseForge cannot " +
                            "change the Alpha/Beta/Release badge after a file is uploaded."
                    )
                    else -> throw GradleException(
                        "publish_type must be stable, beta or alpha (got '" + declaredType + "')"
                    )
                }
            }
        )
        modLoaders.add("forge")
        // Dry run unless explicitly overridden. The default must NOT be derived from whether a token
        // exists: the developer machine is exactly the one that has a token, so "no token means dry
        // run" turns every configuration check into a real upload. Whether an upload really happened
        // can only be told from a fileId in build/publishMods/publishCurseforge.json.
        dryRun.set(findProperty("publish_dry_run")?.toString()?.toBoolean() ?: true)

        curseforge {
            projectId.set(prop("curseforge_project_id"))
            projectSlug.set(prop("curseforge_project_slug"))
            accessToken.set(cfToken)
            minecraftVersions.add(prop("minecraft_version"))
            // CurseForge rejects a file unless at least one side is set.
            client.set(true)
            server.set(true)
            // Runtime requirement, not the compile toolchain: 1.20.1 players run Java 17.
            javaVersions.add(JavaVersion.VERSION_17)
            changelogType.set("markdown")
            requires("tetra")
        }
    }

    // The uploaded name and the jar name are kept identical (<mod>-<base>-<tetra>-<slashblade>), so
    // what the CurseForge file list shows is exactly what players get in their mods folder. No
    // extra "(Tetra 6.x.x)" suffix: the version segment already carries that.
    extensions.configure<ModPublishExtension> {
        displayName.set(prop("mod_name") + "-" + project.version)
    }
}

// MDG remaps each module in a forked JVM whose heap it does not let us set: HotSpot's ergonomic
// default reserves a quarter of physical RAM (8.5G here). Three modules remapping at once, on top of
// the daemon and the compile workers, can exhaust the Windows commit limit and the build dies with
// "insufficient memory... Native memory allocation (malloc) failed", leaving hs_err_pid*.log files
// next to the build. Ordering the remaps keeps at most one of those JVMs alive at a time; everything
// else, including compilation and jarJar, still runs in parallel.
// This lowers the peak but does not cap those JVMs. If such a crash recurs, drop --parallel or lower
// org.gradle.workers.max for that build; nothing in this script can set the remap JVM's -Xmx.
gradle.projectsEvaluated {
    subprojects.mapNotNull { it.tasks.findByName("reobfJar") }
        .sortedBy { it.path }
        .zipWithNext { earlier, later -> later.mustRunAfter(earlier) }
}

subprojects {
    repositories {
        maven("https://cursemaven.com")
        maven("https://www.jitpack.io")
    }

    // configureEach, not configure: a subproject only has the Java extension once its own
    // plugins {} block has run, which happens after this root script is evaluated. The guard is on
    // plugins.withId for the same reason.
    plugins.withId("java") {
        extensions.configure<JavaPluginExtension> {
            // 1.20.1 ships Java 17 to players, so our classes must be Java 17 bytecode.
            // Do not raise this: Java 21 classes fail to load on a Java 17 client.
            toolchain.languageVersion.set(JavaLanguageVersion.of(17))
        }
        tasks.withType<JavaCompile>().configureEach {
            options.encoding = "UTF-8"
        }
    }

    // Dependencies every module shares.
    //
    // add("configName", "...") instead of the usual accessors: inside subprojects{} the Kotlin
    // DSL accessors (modCompileOnly, jarJar, ...) are not generated, so the configuration name has
    // to be a string. Kept inline rather than in buildSrc (needs the Kotlin compiler toolchain,
    // unavailable offline here) or apply(from=) (deprecated, and loses completion as well).
    plugins.withId("net.neoforged.moddev.legacyforge") {
        dependencies {
            add("modCompileOnly", "io.github.llamalad7:mixinextras-common:0.2.0-rc.2")
            add("annotationProcessor", "io.github.llamalad7:mixinextras-common:0.2.0-rc.2")
            add("modImplementation", "io.github.llamalad7:mixinextras-forge:0.2.0-rc.2")
            add("jarJar", "io.github.llamalad7:mixinextras-forge:0.2.0-rc.2")

            // jarJar does not propagate through a project dependency, so each module re-embeds
            // these. Without it the shipped jar loads but MixinExtras/expressionlib are missing.
            add("implementation", "com.github.yiran1457:YiRanExpressionLib:1.0.0-universal:universal")
            add("jarJar", "com.github.yiran1457:YiRanExpressionLib:1.0.0-universal:universal")
        }

        // The Tetra version modules re-embed the pair they actually compile against. :common only
        // compiles against the oldest supported API (tetra_api_file_id), so it never picks a
        // version of its own by accident.
        if (tetraVersion() != null) {
            dependencies {
                // The shared code lives in :common; the version module only holds the seam.
                add("implementation", project(":common"))
                add("modImplementation", "curse.maven:tetra-289712:" + prop("tetra_file_id"))
                add("modImplementation", "curse.maven:mutil-351914:" + prop("mutil_file_id"))

                // In-game block model/state editor, loaded by runClient only. modRuntimeOnly keeps it
                // off the compile classpath and out of the shipped jar (and it is not jarJar'd), so
                // nothing in the mod can accidentally depend on a debug tool.
                add("modRuntimeOnly", "curse.maven:ibe-editor-256822:" + prop("ibe_file_id"))
            }
            configureTetraNode()
        }
    }
}