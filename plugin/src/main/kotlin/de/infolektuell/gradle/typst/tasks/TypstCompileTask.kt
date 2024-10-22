package de.infolektuell.gradle.typst.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.*
import org.gradle.api.provider.*
import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

@Suppress("LeakingThis")
abstract class TypstCompileTask @Inject constructor(private val executor: WorkerExecutor) : DefaultTask() {
    interface SourceDirectories {
        @get:InputFiles
        val data: SetProperty<Directory>
        @get:InputFiles
        val fonts: ListProperty<Directory>
        @get:InputFiles
        val images: SetProperty<Directory>
        @get:InputFiles
        val typst: SetProperty<Directory>
    }

    protected abstract class TypstAction @Inject constructor(private val execOperations: ExecOperations) : WorkAction<TypstAction.Params> {
        interface Params : WorkParameters {
            val executable: Property<String>
            val packagePath: DirectoryProperty
            val packageCachePath: Property<String>
            val document: RegularFileProperty
            val root: Property<String>
            val variables: MapProperty<String, String>
            val fontDirectories: ListProperty<Directory>
            val creationTimestamp: Property<String>
            val useSystemFonts: Property<Boolean>
            val ppi: Property<Int>
            val target: RegularFileProperty
        }

        override fun execute() {
            execOperations.exec { action ->
                action.executable(parameters.executable.get())
                action.args("compile")
                .args("--root", parameters.root.get())
                parameters.fontDirectories.get().forEach { action.args("--font-path", it.asFile.absolutePath) }
                parameters.useSystemFonts.get().not().takeIf { it }?.let { action.args("--ignore-system-fonts") }
                parameters.variables.get().forEach { (k, v) -> action.args("--input", "$k=$v") }
                if (parameters.creationTimestamp.isPresent) action.args("--creation-timestamp", parameters.creationTimestamp.get())
                if (parameters.packagePath.isPresent) action.args("--package-path", parameters.packagePath.asFile.get().absolutePath)
                if (parameters.packageCachePath.isPresent) action.args("--package-cache-path", parameters.packageCachePath.get())
                if (parameters.ppi.isPresent) action.args("--ppi", parameters.ppi.get().toString())
                action.args(parameters.document.get().asFile.absolutePath)
                .args(parameters.target.asFile.get().absolutePath)
            }
        }
    }

    @get:InputDirectory
    abstract val compiler: DirectoryProperty
    @get:Optional
    @get:InputDirectory
    abstract val packagePath: DirectoryProperty
    @get:Optional
    @get:Input
    abstract val packageCachePath: Property<String>
  @get:InputFiles
  abstract val documents: ListProperty<RegularFile>
  @get:Input
  abstract val targetFilenames: ListProperty<String>
    @get:Input
    abstract val root: Property<String>
    @get:Input
    abstract val variables: MapProperty<String, String>
    @get:Optional
    @get:Input
    abstract val creationTimestamp: Property<String>
    @get:Optional
    @get:Input
    abstract val ppi: Property<Int>
    @get:Input
    abstract val useSystemFonts: Property<Boolean>
    @get:Nested
    abstract val sources: SourceDirectories
    @get:OutputDirectory
    abstract val destinationDir: DirectoryProperty
    @get:OutputFiles
    val compiled: Provider<List<RegularFile>> = targetFilenames.zip(destinationDir) { docs, dir -> docs.map { dir.file(it) } }

  @TaskAction
  protected fun compile () {
      val executable = compiler.asFileTree.matching { spec -> spec.include("**/typst", "**/typst.exe") }.singleFile.absolutePath
    val queue = executor.noIsolation()
      documents.get().zip(compiled.get()) { document, targetFile ->
          queue.submit(TypstAction::class.java) { params ->
              params.executable.set(executable)
              params.packagePath.set(packagePath)
              params.packageCachePath.set(packageCachePath)
              params.document.set(document)
              params.root.set(root)
              params.variables.set(variables)
              params.fontDirectories.set(sources.fonts)
              params.useSystemFonts.set(useSystemFonts)
              params.creationTimestamp.set(creationTimestamp)
              params.ppi.set(ppi)
              params.target.set(targetFile)
          }
      }
  }
}
