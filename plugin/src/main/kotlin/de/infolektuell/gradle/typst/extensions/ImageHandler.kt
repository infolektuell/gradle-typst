package de.infolektuell.gradle.typst.extensions

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

abstract class ImageHandler {
    abstract val source: DirectoryProperty
    abstract val converted: DirectoryProperty
    abstract val passthroughFormats: SetProperty<String>
    abstract val outputFormat: Property<String>
}
