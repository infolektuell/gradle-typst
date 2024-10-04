package de.infolektuell.gradle.typst.extensions

import org.gradle.api.Named
import org.gradle.api.file.*
import org.gradle.api.provider.*
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import javax.inject.Inject

abstract class TypstSourceSet @Inject constructor(private val providers: ProviderFactory, private val layout: ProjectLayout) : Named {
    abstract val destinationDir: DirectoryProperty
    abstract val documents: ListProperty<String>
    abstract val inputs: MapProperty<String, String>
    abstract val merged: Property<String>

    abstract val typst: SetProperty<Directory>
    abstract val data: SetProperty<Directory>
    abstract val images: SetProperty<Directory>
    abstract val fonts: SetProperty<Directory>
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
  fun useLocalPackages() {
    typst.add(typstLocalPackages())
  }
  private fun typstLocalPackages(): Provider<Directory> {
    val currentOs = DefaultNativePlatform.getCurrentOperatingSystem()
    val appDataDir = if (currentOs.isMacOsX) {
        layout.projectDirectory.dir(providers.systemProperty("user.home")).map { it.dir("Library/Application Support") }
    } else if (currentOs.isLinux) {
        layout.projectDirectory.dir(providers.environmentVariable("XDG_DATA_HOME"))
    } else {
        layout.projectDirectory.dir(providers.environmentVariable("APPDATA"))
    }
    return appDataDir.map { it.dir("typst/packages") }
  }
}
