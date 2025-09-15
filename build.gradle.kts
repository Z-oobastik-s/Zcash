plugins {
    kotlin("jvm") version "1.9.10"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.zoobastiks"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
    maven("https://repo.essentialsx.net/releases/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("net.essentialsx:EssentialsX:2.20.1")
    
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.10")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

kotlin {
    jvmToolchain(17)
}

tasks {
    shadowJar {
        archiveFileName.set("Zcash-${version}.jar")
        relocate("kotlin", "com.zoobastiks.zcash.kotlin")
    }
    
    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
    
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }
}
