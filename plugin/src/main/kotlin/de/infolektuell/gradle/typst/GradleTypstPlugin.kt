package de.infolektuell.gradle.typst

import de.infolektuell.gradle.typst.extensions.TypstExtension
import de.infolektuell.gradle.typst.providers.GithubLatestRelease
import de.infolektuell.gradle.typst.tasks.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.provider.Provider
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

class GradleTypstPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create(TypstExtension.EXTENSION_NAME, TypstExtension::class.java)
        val latestTypstVersion: Provider<String> = project.providers.provider { GithubLatestRelease.latestGithubRelease("typst", "typst") }
        extension.version.convention(latestTypstVersion)
        val currentOs = DefaultNativePlatform.getCurrentOperatingSystem()
        val currentArch = DefaultNativePlatform.getCurrentArchitecture()
        val osToken = if (currentOs.isLinux) {
            "unknown-linux-musl.tar.xz"
        } else if (currentOs.isMacOsX) {
            "apple-darwin.tar.xz"
        } else {
            "pc-windows-msvc.zip"
        }
        val archToken = if (currentArch.isArm) {
            "aarch64"
        } else {
            "x86_64"
        }
      val downloadTask = project.tasks.register("downloadTypst", DownloadTask::class.java) { task ->
          task.url.convention(extension.version.map { v -> project.uri("https://github.com/typst/typst/releases/download/$v/typst-$archToken-$osToken") })
          task.target.convention(project.layout.buildDirectory.dir("downloads").zip(task.fileName) { dir, file -> dir.file(file) })
      }
        val extractTask = project.tasks.register("extractTypst", ExtractTask::class.java) { task ->
            task.source.convention(downloadTask.flatMap { it.target })
            task.target.convention(project.layout.buildDirectory.dir("tools/typst"))
        }
        extension.compiler.convention(extractTask.flatMap { it.target })
        val dataDir = if (currentOs.isMacOsX) {
            project.layout.projectDirectory.dir(project.providers.systemProperty("user.home")).map { it.dir("Library/Application Support") }
        } else if (currentOs.isLinux) {
            project.layout.projectDirectory.dir(project.providers.systemProperty("user.home")).map { it.dir(".local/share") }
        } else {
            project.layout.projectDirectory.dir(project.providers.environmentVariable("APPDATA"))
        }
        val cacheDir = if (currentOs.isMacOsX) {
            project.layout.projectDirectory.dir(project.providers.systemProperty("user.home")).map { it.dir("Library/Caches") }
        } else if (currentOs.isLinux) {
            project.layout.projectDirectory.dir(project.providers.systemProperty("user.home")).map { it.dir(".cache") }
        } else {
            project.layout.projectDirectory.dir(project.providers.environmentVariable("LOCALAPPDATA"))
        }
        val packagePath = dataDir.map { it.dir("typst/packages") }
        val packageCachePath = cacheDir.map { it.dir("typst/packages") }
        if (packagePath.get().asFile.exists()) extension.localPackages.convention(packagePath)
        extension.sourceSets.configureEach { s ->
            s.format.pdf.enabled.convention(true)
            s.format.png.enabled.convention(false)
            s.format.png.ppi.convention(144)
            s.format.svg.enabled.convention(false)
        }
      project.tasks.withType(TypstCompileTask::class.java).configureEach { task ->
        task.compiler.convention(extension.compiler)
          task.packagePath.set(extension.localPackages)
          task.packageCachePath.set(packageCachePath.get().asFile.absolutePath)
          task.root.convention(project.layout.projectDirectory.asFile.absolutePath)
          task.creationTimestamp.convention(extension.creationTimestamp)
          task.useSystemFonts.convention(false)
      }
      extension.sourceSets.all { s ->
          val title = s.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
          val sourceRoot = project.layout.projectDirectory.dir("src/${s.name}")
          val imagesRoot = sourceRoot.dir("images")
          val typstRoot = sourceRoot.dir("typst")
          s.typst.add(typstRoot)
          s.data.add(sourceRoot.dir("data"))
          s.fonts.add(sourceRoot.dir("fonts"))
          s.images.add(imagesRoot)
          s.destinationDir.convention(project.layout.buildDirectory.dir("typst/${s.name}"))
        val convertImagesTask = project.tasks.register("convert${title}Images", ConvertImagesTask::class.java) { task ->
            task.onlyIf { task.source.get().asFile.exists() }
            task.source.convention(imagesRoot)
            task.target.convention(project.layout.buildDirectory.dir("generated/typst/images/${s.name}"))
            task.format.convention("png")
            task.quality.convention(100)
        }
          s.images.add(convertImagesTask.flatMap { it.target })
          val documentFilesProvider = s.documents.map { docs -> docs.map { typstRoot.file("$it.typ") } }
          val typstTask = project.tasks.register("compile${title}TypstPdf", TypstCompileTask::class.java) { task ->
              val format = s.format.pdf
              task.onlyIf { format.enabled.get() }
              task.onlyIf { s.documents.get().isNotEmpty() }
              task.documents.set(documentFilesProvider)
              task.targetFilenames.set(s.documents.map { docs -> docs.map { "$it.${format.extension}" } })
              task.pdfStandard.set(format.standard)
              task.variables.set(s.inputs)
              task.sources.data.convention(s.data)
              task.sources.fonts.convention(s.fonts)
              task.sources.images.convention(s.images)
              task.sources.typst.convention(s.typst)
              task.destinationDir.convention(s.destinationDir.dir("pdf"))
          }
          project.tasks.register("merge${title}Typst", MergePDFTask::class.java) { task ->
              task.onlyIf { s.format.pdf.merged.isPresent }
              task.documents.set(typstTask.flatMap { it.compiled })
              task.merged.convention(s.format.pdf.merged.zip(s.destinationDir) { name, dir -> dir.file("$name.pdf") })
          }
          project.tasks.register("compile${title}TypstPng", TypstCompileTask::class.java) { task ->
              val format = s.format.png
              task.onlyIf { format.enabled.get() }
              task.onlyIf { s.documents.get().isNotEmpty() }
              task.documents.set(documentFilesProvider)
              task.targetFilenames.set(s.documents.map { docs -> docs.map { "$it-{p}-of-{t}.${format.extension}" } })
              task.ppi.convention(format.ppi)
              task.variables.set(s.inputs)
              task.sources.data.convention(s.data)
              task.sources.fonts.convention(s.fonts)
              task.sources.images.convention(s.images)
              task.sources.typst.convention(s.typst)
              task.destinationDir.convention(s.destinationDir.dir("png"))
          }
          project.tasks.register("compile${title}TypstSvg", TypstCompileTask::class.java) { task ->
              val format = s.format.svg
              task.onlyIf { format.enabled.get() }
              task.onlyIf { s.documents.get().isNotEmpty() }
              task.documents.set(documentFilesProvider)
              task.targetFilenames.set(s.documents.map { docs -> docs.map { "$it-{p}-of-{t}.${format.extension}" } })
              task.variables.set(s.inputs)
              task.sources.data.convention(s.data)
              task.sources.fonts.convention(s.fonts)
              task.sources.images.convention(s.images)
              task.sources.typst.convention(s.typst)
              task.destinationDir.convention(s.destinationDir.dir("svg"))
          }
      }
        val typstCompileTask = project.tasks.register("compileTypst") { task ->
            task.group = "build"
            task.description = "Compile typst documents"
            task.dependsOn(project.tasks.withType(TypstCompileTask::class.java))
            task.dependsOn(project.tasks.withType(MergePDFTask::class.java))
        }
        project.tasks.findByName(BasePlugin.ASSEMBLE_TASK_NAME)?.dependsOn(typstCompileTask)
    }
  companion object {
    const val PLUGIN_NAME = "de.infolektuell.typst"
  }
}
