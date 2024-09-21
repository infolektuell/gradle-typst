package de.infolektuell.gradle.typst.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectList
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.provider.PropertyInternal
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.internal.state.ModelObject
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

abstract class TypstCompileTask @Inject constructor(private val executor: WorkerExecutor, private val objects: ObjectFactory) : DefaultTask() {
  class Document(@Internal private val name: String, objects: ObjectFactory) : Named {
    @get:InputFile
    val source: RegularFileProperty = objects.fileProperty()
    @get:OutputFile
    val target: RegularFileProperty = objects.fileProperty()
    override fun getName() = name
  }
  @get:Nested
  val documents: NamedDomainObjectList<Document> = objects.namedDomainObjectList(Document::class.java)
  fun addDocument(name: String): Document {
    val doc = Document(name, objects)
    // See https://github.com/gradle/gradle/issues/6619
    (doc.target as PropertyInternal<*>).attachProducer(this as ModelObject)
    documents.add(doc)
    return doc
  }
  @get:InputFiles
  val sources: ConfigurableFileCollection = objects.fileCollection()
  @get:Input
  abstract val rootDir: Property<String>
  @get:Input
  abstract val data: MapProperty<String, String>
  @get:Optional
  @get:InputDirectory
  abstract val fontPath: DirectoryProperty
  @get:Input
  abstract val compiler: Property<String>
  @TaskAction
  fun compile () {
    val queue = executor.noIsolation()
    documents.forEach { document ->
      queue.submit(TypstAction::class.java) { params ->
        params.input.set(document.source)
        params.output.set(document.target)
        params.root.set(rootDir)
        if (fontPath.isPresent) params.fontPath.set(fontPath)
        params.inputs.set(data)
        params.compiler.set(compiler)
      }
    }
  }
}
