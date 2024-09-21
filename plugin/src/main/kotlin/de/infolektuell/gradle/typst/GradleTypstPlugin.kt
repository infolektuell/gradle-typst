package de.infolektuell.gradle.typst

import de.infolektuell.gradle.typst.extensions.TypstExtension
import de.infolektuell.gradle.typst.tasks.MergePDFTask
import de.infolektuell.gradle.typst.tasks.TypstCompileTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class GradleTypstPlugin : Plugin<Project> {
    override fun apply(project: Project) {
      val extension = project.extensions.create(TypstExtension.EXTENSION_NAME, TypstExtension::class.java)
      extension.compiler.convention("typst")
      extension.sourceSets.configureEach { s ->
        val sourceRoot = project.layout.projectDirectory.dir("src/${s.name}")
        s.documentsRoot.convention(sourceRoot.dir("typst"))
        s.typst.srcDir(s.documentsRoot)
        s.data.srcDir(sourceRoot.dir("data"))
        s.fonts.srcDir(sourceRoot.dir("fonts"))
        s.images.srcDir(sourceRoot.dir("images"))
        s.useLocalPackages()
        s.destination.convention(project.layout.buildDirectory.dir("typst/${s.name}"))
      }
      project.tasks.withType(TypstCompileTask::class.java).configureEach { task ->
        task.rootDir.convention(project.layout.projectDirectory.asFile.absolutePath)
        task.compiler.convention(extension.compiler)
      }
      extension.sourceSets.all { s ->
        val typstTask = project.tasks.register("${s.name}TypstCompile", TypstCompileTask::class.java) { task ->
          s.documents.get().forEach { name ->
            task.addDocument(name).apply {
              source.set(s.documentsRoot.file("$name.typ"))
              target.set(s.destination.file("$name.pdf"))
            }
          }
          task.sources.from(s.typst - s.documentFiles.files)
          val fontDirs = s.fonts.sourceDirectories.filter { it.exists() }
          if (!fontDirs.isEmpty) task.fontPath.set(fontDirs.first())
          task.data.put("gitHash", project.version.toString())
        }
        val mergeTask = project.tasks.register("${s.name}TypstMerge", MergePDFTask::class.java) { task ->
          typstTask.get().documents.forEach { doc ->
            task.addDocument(doc.name).apply {
              source.set(doc.target)
            }
          }
          task.merged.set(s.destination.file(s.merged.map { "$it.pdf" }))
        }
        project.tasks.register("${s.name}Typst") { task ->
          if (s.documents.get().isEmpty()) {
            task.enabled = false
            return@register
          }
          task.group = "build"
          task.description = "Compile typst documents from ${s.name} source set"
          task.dependsOn(typstTask)
          if (s.merged.isPresent) task.dependsOn(mergeTask)
        }
      }
    }
  companion object {
    const val PLUGIN_NAME = "de.infolektuell.typst"
  }
}
