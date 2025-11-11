package de.infolektuell.gradle.typst.service

import de.infolektuell.gradle.typst.model.DownloadClient
import de.infolektuell.gradle.typst.model.Platform
import de.infolektuell.gradle.typst.model.PropertiesCache
import de.infolektuell.gradle.typst.model.Version
import java.nio.file.Files
import java.nio.file.Path
import java.net.URI

class TypstDataStore {
    private val properties = PropertiesCache()
    private val platform = Platform.getCurrentPlatform()
    private val os = platform.operatingSystem.name.lowercase()
    private val arch = platform.architecture.name.lowercase()

    val executableName = if (platform.operatingSystem == Platform.OperatingSystem.WINDOWS) "typst.exe" else "typst"
    val packageDir: Path get() = platform.operatingSystem.dataDir.resolve("typst/packages")
    val packageCacheDir: Path get() = platform.operatingSystem.cacheDir.resolve("typst/packages")
    val hasPackages: Boolean get() = Files.exists(packageDir)
    fun asset(tag: String) = GithubClient.Asset("typst", "typst", tag, "typst-$arch-$os")

    /** Creates a GitHub release asset for a given Typst [version] reading from a given [config] resource */
    fun asset(version: Version, config: String = "/typst-releases.properties"): DownloadClient.Asset {
        val tag = version.let { "v${it.major}.${it.minor}.${it.patch}" }
        val prefix = "typst.$tag.$arch.$os"
        val data = properties.load(config)
        val url = data.getProperty("$prefix.url")
        val name = data.getProperty("$prefix.name")
        val digest = data.getProperty("$prefix.digest")
        return DownloadClient.Asset(URI.create(url), name, digest)
    }

    /** Creates a GitHub release asset for a given Typst [version] reading from a given [config] file */
    fun asset(version: Version, config: Path): DownloadClient.Asset {
        val tag = version.let { "v${it.major}.${it.minor}.${it.patch}" }
        val prefix = "typst.$tag.$arch.$os"
        val data = properties.load(config)
        val url = data.getProperty("$prefix.url")
        val name = data.getProperty("$prefix.name")
        val digest = data.getProperty("$prefix.digest")
        return DownloadClient.Asset(URI.create(url), name, digest)
    }
}
