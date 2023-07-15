plugins {
  base
}

fun jar(platform: String) = project(":minimotd-$platform")
  .the<MiniMOTDPlatformExtension>().jarTask.flatMap { it.archiveFile }
