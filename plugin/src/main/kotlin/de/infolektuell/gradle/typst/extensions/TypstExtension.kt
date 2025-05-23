package de.infolektuell.gradle.typst.extensions

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class TypstExtension @Inject constructor(objects: ObjectFactory) : ExtensionAware {
    abstract val root: DirectoryProperty
    abstract val dest: DirectoryProperty
    abstract val groups: NamedDomainObjectContainer<GroupHandler>
    val merge: NamedDomainObjectContainer<MergeHandler> = objects.domainObjectContainer(MergeHandler::class.java)
    abstract val executable: RegularFileProperty
  abstract val version: Property<String>
    abstract val localPackages: DirectoryProperty
    abstract val creationTimestamp: Property<String>
    abstract val ignoreSystemFonts: Property<Boolean>
    abstract val pdfStandard: Property<PdfTargetHandler.PdfStandard>
  companion object {
    const val EXTENSION_NAME = "typst"
  }
}
