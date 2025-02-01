enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
    maven("https://maven.neoforged.net/releases")
  }
  includeBuild("build-logic")
}


plugins {
  id("ca.stellardrift.polyglot-version-catalogs") version "6.0.1"
}

rootProject.name = "MiniMOTD"

fun setup(name: String, dir: String) {
  include(name)
  project(":$name").projectDir = file(dir)
}

fun platform(name: String) = setup("minimotd-$name", "platform/$name")
fun dist(name: String) = setup("minimotd-$name", "dist/$name")

setup("minimotd-common", "common")

sequenceOf(
  "neoforge",
).forEach(::platform)
