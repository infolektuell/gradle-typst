package de.infolektuell.gradle.typst.extensions

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property

abstract class TypstExtension {
  abstract val sourceSets: NamedDomainObjectContainer<TypstSourceSet>
    abstract val executable: RegularFileProperty
  abstract val version: Property<String>
    abstract val creationTimestamp: Property<String>
  abstract val localPackages: DirectoryProperty
  companion object {
    const val EXTENSION_NAME = "typst"
  }
}
