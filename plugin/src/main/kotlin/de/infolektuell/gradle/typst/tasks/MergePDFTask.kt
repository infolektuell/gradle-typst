package de.infolektuell.gradle.typst.tasks
import org.apache.pdfbox.multipdf.PDFMergerUtility
import org.gradle.api.DefaultTask
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectList
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.*
import java.nio.file.Files
import javax.inject.Inject

abstract class MergePDFTask @Inject constructor(private val objects: ObjectFactory) : DefaultTask() {
  abstract class Document @Inject constructor(@Internal private val name: String) : Named {
    @get:InputFile
    abstract val source: RegularFileProperty
    override fun getName() = name
  }
  @get:Nested
  val documents: NamedDomainObjectList<Document> = objects.namedDomainObjectList(Document::class.java)
  fun addDocument(name: String): Document {
    val doc = objects.newInstance(Document::class.java, name)
    documents.add(doc)
    return doc
  }
  @get:OutputFile
  abstract val merged: RegularFileProperty
  @TaskAction
  fun merge() {
    Files.newOutputStream(merged.get().asFile.toPath()).use { outFile ->
      PDFMergerUtility().run {
        documents.forEach { addSource(it.source.asFile.get()) }
        destinationStream = outFile
        mergeDocuments(null)
      }
    }
  }
}
