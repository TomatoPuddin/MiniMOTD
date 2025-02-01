import net.neoforged.moddevgradle.dsl.NeoForgeExtension
import java.util.Date
import java.text.SimpleDateFormat

plugins {
  id("net.neoforged.moddev")
  id("minimotd.platform-conventions")
  id("minimotd.shadow-platform")
  id("com.github.johnrengelman.shadow")
}

val shade: Configuration by configurations.creating
val neoforgeVersion: String = libs.versions.neoforge.get()
val gameVersionRange: String = libs.versions.minecraftTarget.get()
val gameVersionRangeH: String = libs.versions.minecraftTargetH.get()
val neoforgeVersionRange: String = libs.versions.neoforgeTarget.get()

configure<NeoForgeExtension> {
  version = neoforgeVersion

  runs {
    create("server") {
      server()
    }
  }
}

dependencies {
  shade(implementation(projects.minimotdCommon){})
}

indra {
  javaVersions {
    target(21)
  }
}

tasks {
  processResources {
    val replaceProperties = mapOf(
      "minecraft_version_range" to gameVersionRange,
      "neoforge_version" to neoforgeVersion,
      "neoforge_version_range" to neoforgeVersionRange,
      "mod_id" to Constants.ID,
      "mod_name" to Constants.DISPLAY_NAME,
      "mod_license" to "MIT",
      "mod_version" to version,
      "mod_authors" to Constants.GITHUB_USER,
      "mod_description" to "",
      "display_url" to Constants.GITHUB_URL,
      "issue_tracker_url" to Constants.GITHUB_ISSUES_URL)

    inputs.properties(replaceProperties)
    filesMatching(listOf("META-INF/neoforge.mods.toml", "pack.mcmeta")) {
      expand(replaceProperties)
    }
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
        "Implementation-Timestamp" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date())
      ))
    }

    exclude {
      it.path.startsWith("META-INF/maven")
        || it.path.startsWith("META-INF/services")
        || it.path.startsWith("META-INF/versions/9")
    }
    archiveFileName.set("minimotd-reforged-$gameVersionRangeH-${project.version}.jar")

    configurations = listOf(shade)
    commonRelocation("io.leangen.geantyref")
    commonRelocation("net.kyori")
  }
}
