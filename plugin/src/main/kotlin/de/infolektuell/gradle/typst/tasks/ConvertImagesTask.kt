package de.infolektuell.gradle.typst.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.FileType
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations
import org.gradle.work.ChangeType
import org.gradle.work.Incremental
import org.gradle.work.InputChanges
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

abstract class ConvertImagesTask @Inject constructor(private val fileSystemOperations: FileSystemOperations, private val executor: WorkerExecutor) : DefaultTask() {
    protected abstract class MagickAction @Inject constructor(private val execOperations: ExecOperations) : WorkAction<MagickAction.Params> {
        interface Params : WorkParameters {
            val source: RegularFileProperty
            val target: RegularFileProperty
            val format: Property<String>
            val quality: Property<Int>
        }

        override fun execute() {
            execOperations.exec { action ->
                action.executable("magick")
                action.args(parameters.source.asFile.get().absolutePath)
                action.args("-format", parameters.format.get())
                action.args("-quality", parameters.quality.get())
                action.args(parameters.target.asFile.get().absolutePath)
            }
        }
    }

  @get:Incremental
  @get:PathSensitive(PathSensitivity.RELATIVE)
  @get:InputDirectory
  abstract val source: DirectoryProperty
  @get:Input
  abstract val format: Property<String>
  @get:Input
  abstract val quality: Property<Int>
  @get:OutputDirectory
  abstract val target: DirectoryProperty
  @TaskAction
  protected fun convert (inputs: InputChanges) {
      source.get().asFileTree.visit { file ->
          if (!file.isDirectory) return@visit
          target.dir(file.relativePath.pathString).get().asFile.mkdirs()
      }
      val supportedFormats = setOf("png", "jpg", "gif", "svg")
      val queue = executor.noIsolation()
      inputs.getFileChanges(source).forEach { change ->
          if (change.fileType == FileType.DIRECTORY) return@forEach
          val targetFile = target.zip(format) { t, f ->
          val fileName = change.normalizedPath.replaceAfterLast('.', f)
              t.file(fileName)
      }
          if (change.changeType == ChangeType.REMOVED) {
              fileSystemOperations.delete { spec ->
                  spec.delete(targetFile)
              }
              return@forEach
          }
          if (supportedFormats.contains(change.file.extension)) {
              fileSystemOperations.copy { spec ->
                  spec.from(change.file)
                  spec.into(targetFile.get().asFile.parent)
              }
              return@forEach
          }
          queue.submit(MagickAction::class.java) { params ->
              params.source.set(change.file)
              params.target.set(targetFile)
              params.format.set(format)
              params.quality.set(quality)
          }
    }
  }
}
