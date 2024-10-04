package de.infolektuell.gradle.typst

import de.infolektuell.gradle.typst.extensions.TypstExtension
import de.infolektuell.gradle.typst.tasks.ConvertImagesTask
import de.infolektuell.gradle.typst.tasks.MergePDFTask
import de.infolektuell.gradle.typst.tasks.TypstCompileTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin

class GradleTypstPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create(TypstExtension.EXTENSION_NAME, TypstExtension::class.java)
      extension.compiler.convention("typst")
      project.tasks.withType(TypstCompileTask::class.java).configureEach { task ->
        task.compiler.convention(extension.compiler)
          task.root.convention(project.layout.projectDirectory.asFile.absolutePath)
      }
      extension.sourceSets.all { s ->
          val sourceRoot = project.layout.projectDirectory.dir("src/${s.name}")
          val typstRoot = sourceRoot.dir("typst")
          s.typst.add(typstRoot)
          s.data.add(sourceRoot.dir("data"))
          s.fonts.add(sourceRoot.dir("fonts"))
          s.images.add(sourceRoot.dir("images"))
          s.useLocalPackages()
          s.destinationDir.convention(project.layout.buildDirectory.dir("typst/${s.name}"))
          s.merged.convention("merged")
        project.tasks.register("convert${s.name}Images", ConvertImagesTask::class.java) { task ->
            task.source.convention(s.images.map { it.first()})
            task.target.convention(project.layout.buildDirectory.dir("generated/magick/images"))
            task.format.convention("png")
            task.quality.convention(100)
            s.images.add(task.target)
        }
          val typstTask = project.tasks.register("compile${s.name}Typst", TypstCompileTask::class.java) { task ->
            task.documents.convention(s.documents.map { docs -> docs.map { typstRoot.file("$it.typ") } })
            task.variables.convention(s.inputs)
            task.sources.data.convention(s.data)
            task.sources.fonts.convention(s.fonts)
            task.sources.images.convention(s.images)
          task.sources.typst.convention(s.typst)
            task.destinationDir.convention(s.destinationDir)
        }
          project.tasks.register("merge${s.name}Typst", MergePDFTask::class.java) { task ->
              task.documents.convention(typstTask.flatMap { it.compiled })
              task.merged.convention(s.destinationDir.file(s.merged.map { "$it.pdf" }))
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
