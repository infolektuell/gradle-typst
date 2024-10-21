package de.infolektuell.gradle.typst.extensions

import org.gradle.api.Action
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested

abstract class TypstOutputFormatExtension {
    abstract class OutputFormat {
        abstract val enabled: Property<Boolean>
    }
    abstract class PDF : OutputFormat() {
        val extension: String get() = "pdf"
        abstract val merged: Property<String>
    }
    abstract class PNG : OutputFormat() {
        val extension: String get() = "png"
        abstract val ppi: Property<Int>
    }
    abstract class SVG : OutputFormat() {
        val extension: String get() = "svg"
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
