package de.infolektuell.gradle.typst.service

import de.infolektuell.gradle.typst.model.Platform
import java.nio.file.Files
import java.nio.file.Path

class TypstDataStore {
    private val platform = Platform.getCurrentPlatform()
    private val os = platform.operatingSystem.name.lowercase()
    private val arch = platform.architecture.name.lowercase()

    val executableName = if (platform.operatingSystem == Platform.OperatingSystem.WINDOWS) "typst.exe" else "typst"
    val packageDir: Path get() = platform.operatingSystem.dataDir.resolve("typst/packages")
    val packageCacheDir: Path get() = platform.operatingSystem.cacheDir.resolve("typst/packages")
    val hasPackages: Boolean get() = Files.exists(packageDir)
    fun asset(tag: String) = GithubClient.Asset("typst", "typst", tag, "typst-$arch-$os")
}
