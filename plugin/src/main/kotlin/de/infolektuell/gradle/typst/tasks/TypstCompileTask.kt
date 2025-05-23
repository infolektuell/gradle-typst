package de.infolektuell.gradle.typst.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.DomainObjectSet
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

abstract class TypstCompileTask @Inject constructor(private val executor: WorkerExecutor) : DefaultTask() {
    sealed interface DocumentConfig {
        @get:InputFile
        val input: RegularFileProperty
        @get:Input
        val inputs: MapProperty<String, String>
        @get:Input
        val ignoreSystemFonts: Property<Boolean>
    }
    interface PdfDocumentConfig : DocumentConfig {
        @get:OutputFile
        val outFile: RegularFileProperty
        @get:Input
        val pdfStandard: Property<String>
    }
    interface PngDocumentConfig : DocumentConfig {
        @get:OutputDirectory
        val outDir: DirectoryProperty
        @get:Input
        val filenameTemplate: Property<String>
        @get:Input
        val ppi: Property<Int>
    }
    interface SvgDocumentConfig : DocumentConfig {
        @get:OutputDirectory
        val outDir: DirectoryProperty
        @get:Input
        val filenameTemplate: Property<String>
    }

    protected abstract class TypstAction @Inject constructor(private val execOperations: ExecOperations) : WorkAction<TypstAction.Params> {
        interface Params : WorkParameters {
            val executable: RegularFileProperty
            val packagePath: DirectoryProperty
            val packageCachePath: Property<String>
            val root: Property<String>
            val inputs: MapProperty<String, String>
            val fontDirectories: SetProperty<Directory>
            val creationTimestamp: Property<String>
            val ignoreSystemFonts: Property<Boolean>
            val ppi: Property<Int>
            val pdfStandard: Property<String>
            val input: RegularFileProperty
            val output: RegularFileProperty
        }

        override fun execute() {
            execOperations.exec { action ->
                action.executable(parameters.executable.get())
                action.args("compile")
                    .args("--root", parameters.root.get())
                parameters.fontDirectories.get().forEach { action.args("--font-path", it.asFile.absolutePath) }
                if (parameters.ignoreSystemFonts.get()) action.args("--ignore-system-fonts")
                parameters.inputs.get().forEach { (k, v) -> action.args("--input", "$k=$v") }
                if (parameters.creationTimestamp.isPresent) action.args("--creation-timestamp", parameters.creationTimestamp.get())
                if (parameters.packagePath.isPresent) action.args("--package-path", parameters.packagePath.asFile.get().absolutePath)
                if (parameters.packageCachePath.isPresent) action.args("--package-cache-path", parameters.packageCachePath.get())
                if (parameters.ppi.isPresent) action.args("--ppi", parameters.ppi.get().toString())
                if (parameters.pdfStandard.isPresent) action.args("--pdf-standard", parameters.pdfStandard.get())
                action.args(parameters.input.get(), parameters.output.get())
            }
        }
    }

    @get:InputFiles
    abstract val includes: ConfigurableFileCollection
    @get:Nested
    abstract val documents: DomainObjectSet<DocumentConfig>
    @get:InputFile
    abstract val executable: RegularFileProperty
    @get:Input
    abstract val root: Property<String>
    @get:Optional
    @get:InputDirectory
    abstract val packagePath: DirectoryProperty
    @get:InputFiles
    abstract val fontDirectories: SetProperty<Directory>
    @get:Optional
    @get:Input
    abstract val packageCachePath: Property<String>
    @get:Optional
    @get:Input
    abstract val creationTimestamp: Property<String>
    @TaskAction
    protected fun compile () {
        val queue = executor.noIsolation()
        documents.forEach { document ->
            queue.submit(TypstAction::class.java) { params ->
                params.executable.set(executable)
                params.root.set(root)
                params.packagePath.set(packagePath)
                params.fontDirectories.set(fontDirectories)
                params.packageCachePath.set(packageCachePath)
                params.creationTimestamp.set(creationTimestamp)
                params.input.set(document.input)
                params.ignoreSystemFonts.set(document.ignoreSystemFonts)
                params.inputs.set(document.inputs)
                when (document) {
                    is PdfDocumentConfig -> {
                        params.output.set(document.outFile)
                        params.pdfStandard.set(document.pdfStandard)
                    }
                    is PngDocumentConfig -> {
                        params.output.set(document.outDir.file(document.filenameTemplate))
                        params.ppi.set(document.ppi)
                    }
                    is SvgDocumentConfig -> {
                        params.output.set(document.outDir.file(document.filenameTemplate))
                    }
                }
            }
        }
    }
}
