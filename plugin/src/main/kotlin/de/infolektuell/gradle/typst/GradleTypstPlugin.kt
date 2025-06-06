package de.infolektuell.gradle.typst

import de.infolektuell.gradle.typst.extensions.TypstExtension
import de.infolektuell.gradle.typst.service.GithubClient
import de.infolektuell.gradle.typst.service.TypstDataStore
import de.infolektuell.gradle.typst.tasks.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import kotlin.io.path.relativeTo

class GradleTypstPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val store = TypstDataStore()
        val extension = project.extensions.create(TypstExtension.EXTENSION_NAME, TypstExtension::class.java)
        extension.version.convention(project.providers.provider { GithubClient().findLatestTag("typst", "typst") })
        val assetProvider = extension.version.map { store.asset(it) }
      val downloadTask = project.tasks.register("downloadTypst", DownloadTask::class.java) { task ->
          task.asset.convention(assetProvider)
          task.target.convention(assetProvider.flatMap { project.layout.buildDirectory.file("downloads/${it.filename}") })
      }
        val extractTask = project.tasks.register("extractTypst", ExtractTask::class.java) { task ->
            task.source.convention(downloadTask.flatMap { it.target })
            task.target.convention(project.layout.buildDirectory.dir("tools/typst"))
        }
        val executableProvider = extractTask.flatMap { it.target }
            .map { dir ->
            dir.asFileTree.matching { spec -> spec.include("**/" + store.executableName) }.singleFile
        }
        extension.executable.convention(project.layout.file(executableProvider))
        if (store.hasPackages) extension.localPackages.convention(project.layout.projectDirectory.dir(store.packageDir.toString()))
        extension.sourceSets.configureEach { s ->
            s.root.convention(project.layout.projectDirectory.dir("src/${s.name}"))
            s.excludePatterns.set(extension.excludePatterns)
            s.destinationDir.convention(project.layout.buildDirectory.dir("typst/${s.name}"))
            s.fonts.convention(s.root.dir("fonts"))
            s.images.source.convention(s.root.dir("images"))
            s.images.converted.convention(project.layout.buildDirectory.dir("generated/typst/images/${s.name}"))
            val relativizedImages = s.images.converted.map { images ->
                val root = project.layout.projectDirectory.asFile.toPath()
                val target = images.asFile.toPath()
                "/" + target.relativeTo(root).toString()
            }
            s.inputs.put("${s.name}-converted-images", relativizedImages)
            s.format.pdf.enabled.convention(true)
            s.format.png.enabled.convention(false)
            s.format.png.ppi.convention(144)
            s.format.svg.enabled.convention(false)
        }
      project.tasks.withType(TypstCompileTask::class.java).configureEach { task ->
          task.executable.convention(extension.executable)
          task.packagePath.set(extension.localPackages)
          task.packageCachePath.set(store.packageCacheDir.toString())
          task.root.convention(project.layout.projectDirectory.asFile.absolutePath)
          task.creationTimestamp.convention(extension.creationTimestamp)
          task.useSystemFonts.convention(false)
      }
      extension.sourceSets.all { s ->
          val title = s.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
          val convertImagesTask = project.tasks.register("convert${title}Images", ConvertImagesTask::class.java) { task ->
              task.onlyIf { task.source.get().asFile.exists() }
              task.source.convention(s.images.source)
              task.target.convention(s.images.converted)
              task.format.convention("png")
              task.quality.convention(100)
          }
          val documentFilesProvider = s.documents.zip(s.root) { docs, root -> docs.map { root.file("typst/$it.typ") } }
          val convertedImagesProvider = convertImagesTask.flatMap { it.target }
          val typstTask = project.tasks.register("compile${title}TypstPdf", TypstCompileTask::class.java) { task ->
              val format = s.format.pdf
              task.onlyIf { format.enabled.get() }
              task.onlyIf { s.documents.get().isNotEmpty() }
              task.documents.set(documentFilesProvider)
              task.targetFilenames.set(s.documents.map { docs -> docs.map { "$it.${format.extension}" } })
              task.pdfStandard.set(format.standard)
              s.includes.forEach { include ->
                  task.variables.putAll(include.inputs)
                  task.includes.from(include.files)
                  task.fontDirectories.add(include.fonts)
              }
              task.variables.putAll(s.inputs)
              task.includes.from(s.files, convertedImagesProvider)
              task.fontDirectories.add(s.fonts)
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
              s.includes.forEach { include ->
                  task.variables.putAll(include.inputs)
                  task.includes.from(include.files)
                  task.fontDirectories.add(include.fonts)
              }
              task.variables.putAll(s.inputs)
              task.includes.from(s.files, convertedImagesProvider)
              task.fontDirectories.add(s.fonts)
              task.destinationDir.convention(s.destinationDir.dir("png"))
          }
          project.tasks.register("compile${title}TypstSvg", TypstCompileTask::class.java) { task ->
              val format = s.format.svg
              task.onlyIf { format.enabled.get() }
              task.onlyIf { s.documents.get().isNotEmpty() }
              task.documents.set(documentFilesProvider)
              task.targetFilenames.set(s.documents.map { docs -> docs.map { "$it-{p}-of-{t}.${format.extension}" } })
              s.includes.forEach { include ->
                  task.variables.putAll(include.inputs)
                  task.includes.from(include.files)
                  task.fontDirectories.add(include.fonts)
              }
              task.variables.putAll(s.inputs)
              task.includes.from(s.files, convertedImagesProvider)
              task.fontDirectories.add(s.fonts)
              task.destinationDir.convention(s.destinationDir.dir("svg"))
          }
      }
        val typstCompileTask = project.tasks.register("compileTypst") { task ->
            task.group = "build"
            task.description = "Compile typst documents"
            task.dependsOn(project.tasks.withType(TypstCompileTask::class.java))
            task.dependsOn(project.tasks.withType(MergePDFTask::class.java))
        }
        project.pluginManager.withPlugin("base") {
            project.tasks.named(BasePlugin.ASSEMBLE_TASK_NAME) { it.dependsOn(typstCompileTask) }
        }
    }
  companion object {
    const val PLUGIN_NAME = "de.infolektuell.typst"
  }
}
