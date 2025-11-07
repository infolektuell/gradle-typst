package de.infolektuell.gradle.typst.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.*
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import org.tukaani.xz.XZInputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.inject.Inject

@DisableCachingByDefault(because = "Extracting an archive is not worth caching")
abstract class ExtractTask @Inject constructor(
    private val fileSystem: FileSystemOperations,
    private val archives: ArchiveOperations
) : DefaultTask() {
    @get:InputFile
    abstract val source: RegularFileProperty

    @get:OutputDirectory
    abstract val target: DirectoryProperty

    @TaskAction
    protected fun extract() {
        fileSystem.delete { spec ->
            spec.delete(target)
        }
        fileSystem.copy { spec ->
            val tree = when (source.get().asFile.extension) {
                "xz" -> xzTree()
                "zip" -> archives.zipTree(source)
                else -> archives.tarTree(source)
            }
            spec.from(tree)
            spec.into(target)
        }
    }

    private fun xzTree(): FileTree {
        val xz = XZInputStream(Files.newInputStream(source.get().asFile.toPath()))
        val tarFile = Files.createTempFile("typst", ".tar")
        Files.copy(xz, tarFile, StandardCopyOption.REPLACE_EXISTING)
        return archives.tarTree(tarFile)
    }
}
