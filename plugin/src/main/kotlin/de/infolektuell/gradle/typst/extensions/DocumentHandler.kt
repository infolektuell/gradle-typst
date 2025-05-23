package de.infolektuell.gradle.typst.extensions

import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class DocumentHandler @Inject constructor(objects: ObjectFactory) : Named, ExtensionAware {
    abstract val input: RegularFileProperty
    abstract val pdfStandard: Property<PdfTargetHandler.PdfStandard>
    val targets: ExtensiblePolymorphicDomainObjectContainer<TargetHandler> = objects.polymorphicDomainObjectContainer(TargetHandler::class.java)
        .apply {
            registerFactory(PdfTargetHandler::class.java) { objects.newInstance(PdfTargetHandler::class.java, it) }
            registerFactory(PngTargetHandler::class.java) { objects.newInstance(PngTargetHandler::class.java, it) }
            registerFactory(SvgTargetHandler::class.java) { objects.newInstance(SvgTargetHandler::class.java, it) }
        }
        .also { extensions.add("targets", it) }
    val pdf: NamedDomainObjectContainer<PdfTargetHandler> = targets.containerWithType(PdfTargetHandler::class.java).also { extensions.add("pdf", it) }
    val png: NamedDomainObjectContainer<PngTargetHandler> = targets.containerWithType(PngTargetHandler::class.java).also { extensions.add("png", it) }
    val svg: NamedDomainObjectContainer<SvgTargetHandler> = targets.containerWithType(SvgTargetHandler::class.java).also { extensions.add("svg", it) }
    abstract val inputs: MapProperty<String, String>
    abstract val ignoreSystemFonts: Property<Boolean>
}
