package de.infolektuell.gradle.typst.extensions

import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectSet
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileTree
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Provider
import javax.inject.Inject

abstract class SourceSetHandler @Inject constructor(objects: ObjectFactory) : Named {
    private val title get() = name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    val pdfCompileTaskName get() = "compile${title}TypstPdf"
    val pngCompileTaskName get() = "compile${title}TypstPng"
    val svgCompileTaskName get() = "compile${title}TypstSvg"
    val convertImagesTaskName get() = "convert${title}Images"
    val includes: NamedDomainObjectSet<SourceSetHandler> = objects.namedDomainObjectSet(SourceSetHandler::class.java)
    fun includes(action: Action<in NamedDomainObjectSet<SourceSetHandler>>) {
        action.execute(includes)
    }
    val root: DirectoryProperty = objects.directoryProperty()
    val excludePatterns: ListProperty<String> = objects.listProperty(String::class.java)
    val files: Provider<FileTree> = root.zip(excludePatterns) { r, x -> r.asFileTree.matching { it.exclude(x) }}
    abstract val destinationDir: DirectoryProperty
    abstract val documents: ListProperty<String>
    abstract val inputs: MapProperty<String, String>

    val fonts: DirectoryProperty = objects.directoryProperty()
    val images: ImageHandler = objects.newInstance(ImageHandler::class.java)
    fun images(action: Action<in ImageHandler>) {
        action.execute(images)
    }
    val format: OutputFormatHandler = objects.newInstance(OutputFormatHandler::class.java)
    fun format(action: Action<in OutputFormatHandler>) {
        action.execute(format)
    }

  fun addSourceSet(sourceSet: SourceSetHandler): SourceSetHandler {
    includes.add(sourceSet)
      return this
  }
    fun addSourceSet(sourceSet: Provider<SourceSetHandler>): SourceSetHandler {
        includes.addLater(sourceSet)
        return this
    }
}
