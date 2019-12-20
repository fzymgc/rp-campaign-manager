pluginManagement {
  val kotlinVersion: String by settings
  plugins {
    kotlin("jvm") version kotlinVersion
    kotlin("kapt") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.allopen") version kotlinVersion
    id("com.google.cloud.tools.jib") version "1.8.0"
    id("com.github.johnrengelman.shadow") version "5.2.0"
    id("application")
  }
}

rootProject.name="rp-campaign-manager"
