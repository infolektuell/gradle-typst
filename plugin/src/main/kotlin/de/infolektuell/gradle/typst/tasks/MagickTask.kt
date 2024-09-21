package de.infolektuell.gradle.typst.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileType
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.work.ChangeType
import org.gradle.work.Incremental
import org.gradle.work.InputChanges
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

abstract class MagickTask @Inject constructor(private val executor: WorkerExecutor) : DefaultTask() {
  @get:Incremental
  @get:PathSensitive(PathSensitivity.NAME_ONLY)
  @get:InputDirectory
  abstract val source: DirectoryProperty
  @get:Input
  abstract val format: Property<String>
  @get:Input
  abstract val quality: Property<Int>
  @get:OutputDirectory
  abstract val target: DirectoryProperty
  @TaskAction
  fun convert (inputs: InputChanges) {
    val queue = executor.noIsolation()
    inputs.getFileChanges(source).forEach { change ->
      if (change.fileType == FileType.DIRECTORY) return@forEach
      val targetFile = target.file(change.normalizedPath).get().asFile.resolveSibling(change.file.nameWithoutExtension + "." + format.get())
      if (change.changeType == ChangeType.REMOVED) {
        targetFile.delete()
      } else {
        queue.submit(MagickAction::class.java) { params ->
          params.source.set(change.file)
          params.target.set(targetFile)
          params.format.set(format)
          params.quality.set(quality)
        }
      }
    }
    queue.await()
  }
}
