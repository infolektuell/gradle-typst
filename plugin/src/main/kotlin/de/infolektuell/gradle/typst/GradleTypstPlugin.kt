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
      project.tasks.withType(TypstCompileTask::class.java).configureEach { task ->
        task.compiler.convention(extension.compiler)
          task.root.convention(project.layout.projectDirectory.asFile.absolutePath)
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
          s.useLocalPackages()
          s.destinationDir.convention(project.layout.buildDirectory.dir("typst/${s.name}"))
        val convertImagesTask = project.tasks.register("convert${title}Images", ConvertImagesTask::class.java) { task ->
            task.onlyIf { task.source.get().asFile.exists() }
            task.source.convention(imagesRoot)
            task.target.convention(project.layout.buildDirectory.dir("generated/typst/images/${s.name}"))
            task.format.convention("png")
            task.quality.convention(100)
        }
          s.images.add(convertImagesTask.flatMap { it.target })
          val typstTask = project.tasks.register("compile${title}Typst", TypstCompileTask::class.java) { task ->
            task.onlyIf { s.documents.get().isNotEmpty() }
              task.documents.convention(s.documents.map { docs -> docs.map { typstRoot.file("$it.typ") } })
            task.variables.convention(s.inputs)
            task.sources.data.convention(s.data)
            task.sources.fonts.convention(s.fonts)
            task.sources.images.convention(s.images)
          task.sources.typst.convention(s.typst)
            task.destinationDir.convention(s.destinationDir)
        }
          project.tasks.register("merge${title}Typst", MergePDFTask::class.java) { task ->
              task.onlyIf { s.merged.isPresent }
              task.documents.convention(typstTask.flatMap { it.compiled })
              task.merged.convention(s.merged.zip(s.destinationDir) { name, dir -> dir.file("$name.pdf") })
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
