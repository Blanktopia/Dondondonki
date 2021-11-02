@file:Suppress("SpellCheckingInspection")

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.0"
    kotlin("plugin.serialization") version "1.5.0"
    id("net.minecrell.plugin-yml.bukkit")
    id("com.github.johnrengelman.shadow")
}

group = "me.weiwen.dondondonki"
version = "1.0.0"

repositories {
    jcenter()
    mavenCentral()

    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
    maven { url = uri("https://papermc.io/repo/repository/maven-public") }
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }

    // bStats
    maven { url = uri("https://repo.codemc.org/repository/maven-public") }

    // MineDown
    maven { url = uri("https://repo.minebench.de/") }

    // EssentialsX
    maven { url = uri("https://repo.essentialsx.net/releases/") }

    // GriefPrevention
    maven { url = uri("https://jitpack.io") }

    // Lib's Disguises
    maven { url = uri("https://repo.md-5.net/content/groups/public/") }

    // ProtocolLib
    maven { url = uri("https://repo.dmulloy2.net/repository/public/") }

    mavenLocal()
}

dependencies {
    implementation(kotlin("stdlib-jdk8", "1.5.0"))

    // Deserialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1")
    implementation("com.charleskorn.kaml:kaml:0.33.0")

    // Paper
    compileOnly("io.papermc.paper", "paper-api", "1.17.1-R0.1-SNAPSHOT")

    // Spigot
    compileOnly("org.spigotmc", "spigot", "1.17.1-R0.1-SNAPSHOT")

    // bStats
    implementation("org.bstats", "bstats-bukkit", "1.8")

    // MineDown
    implementation("de.themoep", "minedown", "1.7.0-SNAPSHOT")

    // EssentialsX
    compileOnly("net.ess3", "EssentialsX", "2.18.2")

    // Moromoro
    compileOnly("me.weiwen.moromoro", "Moromoro", "1.0.0-SNAPSHOT")

    // ProtocolLib
    compileOnly("com.comphenix.protocol", "ProtocolLib", "4.7.0-SNAPSHOT")

    // GriefPrevention
    compileOnly("com.github.TechFortress:GriefPrevention:16.7.1")
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

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.languageVersion = "1.5"
    kotlinOptions.freeCompilerArgs = listOf(
        "-Xopt-in=kotlin.RequiresOptIn",
        "-Xuse-experimental=org.jetbrains.kotlinx.serialization.ExperimentalSerializationApi"
    )
}

tasks.withType<ShadowJar> {
    classifier = null
}
