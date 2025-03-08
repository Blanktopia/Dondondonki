@file:Suppress("SpellCheckingInspection")

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.1.10"
    kotlin("plugin.serialization") version "2.1.10"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    id("io.github.goooler.shadow") version "8.1.7"
}

group = "me.weiwen.dondondonki"
version = "1.0.0"

repositories {
    mavenLocal()
    mavenCentral()

    maven("https://repo.purpurmc.org/snapshots")
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }

    // bStats
    maven { url = uri("https://repo.codemc.org/repository/maven-public") }

    // MineDown
    maven { url = uri("https://repo.minebench.de/") }

    // EssentialsX
    maven { url = uri("https://repo.essentialsx.net/snapshots/") }

    // GriefPrevention
    maven { url = uri("https://jitpack.io") }

    // Lib's Disguises
    maven { url = uri("https://repo.md-5.net/content/groups/public/") }

    // ProtocolLib
    maven { url = uri("https://repo.dmulloy2.net/repository/public/") }

    mavenLocal()
}

dependencies {
    compileOnly(kotlin("stdlib"))

    // Deserialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
    implementation("com.charleskorn.kaml:kaml:0.72.0")

    // Paper
    compileOnly("org.purpurmc.purpur", "purpur-api", "1.21.4-R0.1-SNAPSHOT")

    // EssentialsX
    compileOnly("net.essentialsx", "EssentialsX", "2.21.0-SNAPSHOT")

    // Moromoro
    compileOnly("me.weiwen.moromoro", "Moromoro", "1.2.0-SNAPSHOT")

    // ProtocolLib
    compileOnly("com.comphenix.protocol", "ProtocolLib", "5.1.0")

    // GriefPrevention
    compileOnly("com.github.TechFortress:GriefPrevention:16.18.2")
}

configurations.all {
    resolutionStrategy {
        capabilitiesResolution.withCapability("org.spigotmc:spigot-api:1.21.4-R0.1-SNAPSHOT") {
            select("org.purpurmc.purpur:purpur-api:1.21.4-R0.1-SNAPSHOT")
        }
    }
}

bukkit {
    main = "me.weiwen.dondondonki.Dondondonki"
    name = "Dondondonki"
    version = "1.0.0"
    description = "HermitCraft-like chest shop system"
    apiVersion = "1.16"
    author = "Goh Wei Wen <goweiwen@gmail.com>"
    website = "weiwen.me"

    depend = listOf("Essentials", "ProtocolLib")
    softDepend = listOf("Moromoro", "GriefPrevention")

    commands {
        register("dondondonki") {
            description = "Manages the Dondondonki plugin"
            usage = "/<command> reload"
            permission = "dondondonki.admin"
        }
        register("shop") {
            description = "Commands related to shops"
            usage = "/<command>"
            permission = "dondondonki.command.shop"
        }
    }
}
