package de.infolektuell.gradle.typst.service

import de.infolektuell.gradle.typst.model.DownloadClient
import de.infolektuell.gradle.typst.model.Version
import org.gradle.api.Action
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.process.ExecOperations
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.PathMatcher
import javax.inject.Inject
import kotlin.io.path.absolute

abstract class TypstDownloadService @Inject constructor(
    private val fileSystem: FileSystemOperations,
    private val archives: ArchiveOperations,
    private val execOperations: ExecOperations
) : BuildService<TypstDownloadService.Parameters> {

    interface Parameters : BuildServiceParameters {
        val cacheDir: DirectoryProperty
        val releaseConfiguration: RegularFileProperty
    }

    data class Installation(val root: Path, val executable: Path, val version: Version)
    data class RemoteInstallation(val asset: DownloadClient.Asset, val archive: Path, val installation: Installation)

    private val dataStore = TypstDataStore()
    private val downloadClient = DownloadClient()
    private val downloadsDir = parameters.cacheDir.dir("downloads")
    private val installDir = parameters.cacheDir.dir("installation")
    private val remoteInstallations = mutableMapOf<Version, RemoteInstallation>()
    private val localInstallations = mutableMapOf<Path, Installation>()

    /** Returns the version of a given [local installation][path], null if this is not installed */
    fun version(path: Path) = localInstallations[path]?.version ?: install(path).version

    /**
     * Executes Typst of a given [version] with a configurable [action]
     *
     * This is intended to be used by tasks.
     */
    fun exec (version: Version, action: Action<in ExecSpec>): ExecResult{
        val data = remoteInstallations[version] ?: install(version)
        return execOperations.exec { spec ->
            spec.executable(data.installation.executable.absolute())
            action.execute(spec)
        }
    }

    /**
     * Executes Typst installed in a custom [path][root] with a configurable [action]
     *
     * This is intended to be used by tasks.
     */
    fun exec (root: Path, action: Action<in ExecSpec>): ExecResult {
        val data = localInstallations[root] ?: install(root)
        execOperations.exec(action)
        return execOperations.exec { spec ->
            spec.executable(data.executable.absolute())
            spec.args()
            action.execute(spec)
        }
    }

    private fun install(root: Path): Installation {
        return localInstallations.computeIfAbsent(root) { k ->
            val executable = findExecutable(k)
            val version = ByteArrayOutputStream().use { s ->
                execOperations.exec { spec ->
                    spec.executable(executable)
                    spec.args("--version")
                    spec.errorOutput = s
                }
                s.toString(Charset.defaultCharset())
                    .trim()
                    .lines()
                    .first()
                    .split(" ")[1]
                    .let { Version.parse(it) }
            }
            Installation(k, executable, version)
        }
    }

    private fun install(version: Version): RemoteInstallation {
        return remoteInstallations.computeIfAbsent(version) { k ->
            val asset = parameters.releaseConfiguration.orNull
                ?.let { dataStore.asset(k, it.asFile.toPath()) }
                ?: dataStore.asset(k)
            val archive = downloadClient.download(asset, downloadsDir.get().asFile.toPath())
            val root = installDir.get().asFile.toPath().resolve("$k")
            fileSystem.copy { spec ->
                spec.from(archives.tarTree(archive.toFile()))
                spec.into(root)
            }
            val executable = findExecutable(root)
            val installation = Installation(root, executable, k)
            RemoteInstallation(asset, archive, installation)
        }
    }

    private fun uninstall(version: Version): Boolean {
        val data = remoteInstallations[version] ?: return false
        fileSystem.delete { spec ->
            spec.delete(data.installation.root)
            spec.delete(data.archive)
        }
        remoteInstallations.remove(version)
        return true
    }

    private fun clear() {
        remoteInstallations.keys.forEach { uninstall(it) }
    }

    private fun findExecutable(path: Path): Path {
        val glob = "glob:**/${dataStore.executableName}"
        val pathMatcher: PathMatcher = FileSystems.getDefault().getPathMatcher(glob)
        return Files.walk(path)
            .filter(pathMatcher::matches)
            .filter { Files.isExecutable(it) && Files.isRegularFile(it) }
            .findFirst()
            .takeIf { it.isPresent }
            ?.get()
            ?: throw RuntimeException("Executable not found in $path")
    }

    companion object {
        const val SERVICE_NAME = "typstDownloadService"
    }
}
