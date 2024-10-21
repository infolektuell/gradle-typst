package de.infolektuell.gradle.typst.extensions

import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Nested

abstract class TypstSourceSet : Named {
    abstract val destinationDir: DirectoryProperty
    abstract val documents: ListProperty<String>
    abstract val inputs: MapProperty<String, String>

    abstract val typst: SetProperty<Directory>
    abstract val data: SetProperty<Directory>
    abstract val images: SetProperty<Directory>
    abstract val fonts: SetProperty<Directory>
    @get:Nested
    abstract val format: TypstOutputFormatExtension
    fun format(action: Action<in TypstOutputFormatExtension>) {
        action.execute(format)
    }

  fun addSourceSet(sourceSet: TypstSourceSet) {
    typst.addAll(sourceSet.typst)
    data.addAll(sourceSet.data)
    images.addAll(sourceSet.images)
    fonts.addAll(sourceSet.fonts)
  }
  fun addSourceSet(sourceSet: Provider<TypstSourceSet>) {
      typst.addAll(sourceSet.flatMap { it.typst })
      data.addAll(sourceSet.flatMap { it.data })
      images.addAll(sourceSet.flatMap { it.images })
      fonts.addAll(sourceSet.flatMap { it.fonts })
  }
}
