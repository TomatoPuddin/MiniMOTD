import net.minecraftforge.gradle.userdev.UserDevExtension
import java.util.Date
import java.text.SimpleDateFormat

plugins {
  id("net.minecraftforge.gradle")
  id("org.spongepowered.mixin") apply true
  id("minimotd.platform-conventions")
  id("minimotd.shadow-platform")
  id("com.github.johnrengelman.shadow")
}

val shade: Configuration by configurations.creating
val gameVersion: String = libs.versions.minecraft.get()
val loaderVersion: String = libs.versions.forge.get()
val gameVersionRangeH: String = libs.versions.minecraftTargetH.get()

configure<UserDevExtension> {
  mappings(libs.versions.mappingChannel.get(), libs.versions.mappingVersion.get())

  // accessTransformer(file("src/main/resources/META-INF/accesstransformer.cfg"))
  copyIdeResources.set(true)

  runs {
    create("server") {
      workingDirectory(file("run"))

      taskName = "server"

      property("forge.logging.markers", "SCAN,REGISTRIES,REGISTRYDUMP")
      property("forge.logging.console.level", "debug")

      mods {
        create(Constants.ID) {
          source(sourceSets["main"])
          source(sourceSets["test"])
        }
      }
    }
  }
}

configure<org.spongepowered.asm.gradle.plugins.MixinExtension> {
  add(sourceSets["main"], "${Constants.ID}.refmap.json")
  config("${Constants.ID}.mixins.json")
}

dependencies {
  minecraft("net.minecraftforge:forge:${gameVersion}-${loaderVersion}")

  shade(implementation(projects.minimotdCommon){})
  compileOnly(libs.mixinBooter)

  annotationProcessor(variantOf(libs.mixin){
    classifier("processor")
  })
}

indra {
  javaVersions {
    target(8)
  }
}

tasks {
  processResources {
    val replaceProperties = mapOf(
      "minecraft_version" to gameVersion,
      "forge_version" to loaderVersion,
      "mod_id" to Constants.ID,
      "mod_name" to Constants.DISPLAY_NAME,
      "mod_license" to "MIT",
      "mod_version" to version,
      "mod_authors" to Constants.GITHUB_USER,
      "mod_description" to "",
      "display_url" to Constants.GITHUB_URL,
      "issue_tracker_url" to Constants.GITHUB_ISSUES_URL)

    inputs.properties(replaceProperties)
    filesMatching(listOf("mcmod.info", "pack.mcmeta")) {
      expand(replaceProperties)
    }
  }

  assemble {
    dependsOn(shadowJar)
  }

  reobf {
    create(shadowJar.name)
  }

  shadowJar {
    manifest {
      attributes.putAll(mapOf(
        "Specification-Title"      to Constants.ID,
        "Specification-Vendor"     to Constants.GITHUB_USER,
        "Specification-Version"    to "1", // We are version 1 of ourselves
        "Implementation-Title"     to Constants.DISPLAY_NAME,
        "Implementation-Version"   to version,
        "Implementation-Vendor"    to Constants.GITHUB_USER,
        "Implementation-Timestamp" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date()),
        "TweakClass"               to "org.spongepowered.asm.launch.MixinTweaker",
        "FMLCorePlugin"            to "xyz.jpenilla.minimotd.forge.CoreMod",
        "ForceLoadAsMod"           to "true",
        "FMLCorePluginContainsFMLMod" to "true",
      ))
    }

    archiveFileName.set("minimotd-reforged-$gameVersionRangeH-${project.version}.jar")

    configurations = listOf(shade)
    exclude { el -> el.relativePath.pathString.startsWith("META-INF/versions/") }
    commonRelocation("io.leangen.geantyref")
    commonRelocation("net.kyori")
    finalizedBy("reobfShadowJar")
  }
}
