pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
        maven("https://maven.minecraftforge.net/") { name = "MinecraftForge" }
        maven("https://maven.parchmentmc.org") { name = "ParchmentMC" }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
    id("dev.kikugie.stonecutter") version "0.9.8"
}

stonecutter.create(rootProject) {
    versions(mapOf("6.9.0" to "6.9.0", "6.17.0" to "6.17.0"))
    vcsVersion.set("6.9.0")
}

rootProject.name = "SlashBladeTetra"
