plugins {
  id("minimotd.build-logic")
}

allprojects {
  repositories {
    gradlePluginPortal()
    mavenCentral()
    maven("https://maven.blamejared.com")
  }
}
