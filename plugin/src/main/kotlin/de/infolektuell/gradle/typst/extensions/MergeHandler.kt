package de.infolektuell.gradle.typst.extensions

import org.gradle.api.DomainObjectSet
import org.gradle.api.Named
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionAware
import javax.inject.Inject

abstract class MergeHandler @Inject constructor(objects: ObjectFactory) : Named, ExtensionAware {
    val source: DomainObjectSet<PdfTargetHandler> = objects.domainObjectSet(PdfTargetHandler::class.java).also { extensions.add("merge", it) }
    abstract val target: RegularFileProperty
}
