package de.infolektuell.gradle.typst.extensions

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property

abstract class TypstExtension {
  abstract val sourceSets: NamedDomainObjectContainer<TypstSourceSet>
  abstract val version: Property<String>
    abstract val creationTimestamp: Property<String>
  abstract val compiler: DirectoryProperty
  companion object {
    const val EXTENSION_NAME = "typst"
  }
}
