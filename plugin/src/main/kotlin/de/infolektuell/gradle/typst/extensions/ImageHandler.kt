package de.infolektuell.gradle.typst.extensions

import org.gradle.api.file.DirectoryProperty

abstract class ImageHandler {
    abstract val source: DirectoryProperty
    abstract val converted: DirectoryProperty
}
