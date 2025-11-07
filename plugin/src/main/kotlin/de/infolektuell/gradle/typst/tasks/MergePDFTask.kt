package de.infolektuell.gradle.typst.tasks

import org.apache.pdfbox.multipdf.PDFMergerUtility
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.nio.file.Files

abstract class MergePDFTask : DefaultTask() {
    @get:InputFiles
    abstract val documents: ListProperty<RegularFile>

    @get:OutputFile
    abstract val merged: RegularFileProperty

    @TaskAction
    protected fun merge() {
        Files.newOutputStream(merged.get().asFile.toPath()).use { outFile ->
            PDFMergerUtility().run {
                documents.get().forEach { addSource(it.asFile) }
                destinationStream = outFile
                mergeDocuments(null)
            }
        }
    }
}
