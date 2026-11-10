pluginManagement {
    repositories {
        gradlePluginPortal()
        // Mirror first: me.modmuss50.mod-publish-plugin is not on the Forge/NeoForged repos, and the
        // Gradle plugin portal is unreachable from some networks (its marker pom lives only there).
        maven("https://maven.aliyun.com/repository/gradle-plugin") { name = "AliyunGradlePlugins" }
        maven("https://maven.minecraftforge.net/") { name = "MinecraftForge" }
        maven("https://maven.neoforged.net/releases") { name = "NeoForged" }
    }

    resolutionStrategy {
        eachPlugin {
            // Resolve the module directly instead of through its portal marker pom: the marker is only
            // published to the Gradle plugin portal, so a machine that cannot reach it (or that has
            // the plugin cached but not the marker) otherwise fails with "Plugin was not found".
            if (requested.id.id == "me.modmuss50.mod-publish-plugin") {
                useModule("me.modmuss50:mod-publish-plugin:" + requested.version)
            }
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}

rootProject.name = "SlashBladeTetra"

// One module per supported Tetra version. A version module is recognised by its own
// gradle.properties (tetra_version and the two curse file ids), so adding a version means:
// add the folder here, give it a 3-line build script and a 4-line gradle.properties.
include("common")
include("tetra-6.9.0")
include("tetra-6.17.0")
