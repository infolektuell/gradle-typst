package de.infolektuell.gradle.typst.extensions
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.provider.Property

abstract class TypstExtension {
  abstract val sourceSets: NamedDomainObjectContainer<TypstSourceSet>
  abstract val compiler: Property<String>
  companion object {
    const val EXTENSION_NAME = "typst"
  }
}
