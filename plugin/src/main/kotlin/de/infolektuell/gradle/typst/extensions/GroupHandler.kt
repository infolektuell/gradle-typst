package de.infolektuell.gradle.typst.extensions

import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

abstract class GroupHandler : Named {
    abstract val root: DirectoryProperty
    abstract val includes: SetProperty<Directory>
    abstract val documents: NamedDomainObjectContainer<DocumentHandler>
    abstract val inputs: MapProperty<String, String>
    abstract val ppi: Property<Int>
    abstract val pdfStandard: Property<PdfTargetHandler.PdfStandard>
    abstract val ignoreSystemFonts: Property<Boolean>
}
