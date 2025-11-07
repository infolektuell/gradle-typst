package de.infolektuell.gradle.typst.extensions

import org.gradle.api.Action
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested

abstract class OutputFormatHandler {
    abstract class OutputFormat {
        abstract val enabled: Property<Boolean>
    }

    abstract class PDF : OutputFormat() {
        val extension: String get() = "pdf"

        // 1.7, a-2b, or a-3b
        abstract val standard: Property<String>
        abstract val output: MapProperty<String, RegularFile>
        abstract val merged: Property<String>
    }

    abstract class PNG : OutputFormat() {
        val extension: String get() = "png"
        abstract val ppi: Property<Int>
        abstract val filenameTemplate: Property<String>
        abstract val output: MapProperty<String, Directory>
    }

    abstract class SVG : OutputFormat() {
        val extension: String get() = "svg"
        abstract val filenameTemplate: Property<String>
        abstract val output: MapProperty<String, Directory>
    }

    @get:Nested
    abstract val pdf: PDF
    fun pdf(action: Action<in PDF>) {
        action.execute(pdf)
    }

    @get:Nested
    abstract val png: PNG
    fun png(action: Action<in PNG>) {
        action.execute(png)
    }

    @get:Nested
    abstract val svg: SVG
    fun svg(action: Action<in SVG>) {
        action.execute(svg)
    }
}
