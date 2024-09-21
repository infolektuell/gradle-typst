package de.infolektuell.gradle.typst.extensions
import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.file.*
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import javax.inject.Inject

abstract class TypstSourceSet @Inject constructor(objects: ObjectFactory, private val layout: ProjectLayout) : Named {
  abstract val documentsRoot: DirectoryProperty
  abstract val documents: SetProperty<String>
  val documentFiles = documentsRoot.files(documents.map { it.map { file -> file + ".typ" } })
  val destination: DirectoryProperty = objects.directoryProperty()
  abstract val merged: Property<String>
  val typst: SourceDirectorySet = objects.sourceDirectorySet("typst", "Typst files").apply {
    include("*.typ")
    destinationDirectory.set(destination)
  }
  fun typst(action: Action<in SourceDirectorySet>) {
    action.execute(typst)
  }
  val data: SourceDirectorySet = objects.sourceDirectorySet("data", "Data files").apply {
    include("*.json", "*.yml", "*.yaml", "*.toml")
    destinationDirectory.set(destination)
  }
  fun data(action: Action<in SourceDirectorySet>) {
    action.execute(data)
  }
  val images: SourceDirectorySet = objects.sourceDirectorySet("data", "Data files").apply {
    include("*.png", "*.jpg", "*.gif", "*.svg")
    destinationDirectory.set(destination)
  }
  fun images(action: Action<in SourceDirectorySet>) {
    action.execute(images)
  }
  val fonts: SourceDirectorySet = objects.sourceDirectorySet("fonts", "Font files").apply {
    include("*.ttf", "*.ttc", "*.otf", "*.otc")
    destinationDirectory.set(destination)
  }
  fun fonts(action: Action<in SourceDirectorySet>) {
    action.execute(fonts)
  }
  fun addSourceSet(sourceSet: TypstSourceSet) {
    typst.srcDirs(sourceSet.typst.sourceDirectories)
    data.srcDirs(sourceSet.data.sourceDirectories)
    images.srcDirs(sourceSet.images.sourceDirectories)
    fonts.srcDirs(sourceSet.fonts.sourceDirectories)
  }
  fun addSourceSet(sourceSet: Provider<TypstSourceSet>) {
    typst.srcDirs(sourceSet.map { it.typst.sourceDirectories })
    data.srcDirs(sourceSet.map { it.data.sourceDirectories })
    images.srcDirs(sourceSet.map { it.images.sourceDirectories })
    fonts.srcDirs(sourceSet.map { it.fonts.sourceDirectories })
  }
  fun useLocalPackages() {
    typst.srcDir(typstLocalPackages())
  }
  private fun typstLocalPackages(): Directory {
    val homeDir = layout.projectDirectory.dir(System.getProperty("user.home"))
    val currentOs = DefaultNativePlatform.getCurrentOperatingSystem()
    val path = if (currentOs.isMacOsX) {
      "Library/Application Support"
    } else if (currentOs.isLinux) {
      System.getenv("XDG_DATA_HOME")
    } else {
      System.getenv("APPDATA")
    }
    return homeDir.dir(path).dir("typst/packages")
  }
}
