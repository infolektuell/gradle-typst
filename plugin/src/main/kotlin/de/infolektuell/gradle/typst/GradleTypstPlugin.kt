package de.infolektuell.gradle.typst

import de.infolektuell.gradle.typst.extensions.PdfTargetHandler.PdfStandard
import de.infolektuell.gradle.typst.extensions.TypstExtension
import de.infolektuell.gradle.typst.service.GithubClient
import de.infolektuell.gradle.typst.service.TypstDataStore
import de.infolektuell.gradle.typst.tasks.DownloadTask
import de.infolektuell.gradle.typst.tasks.ExtractTask
import de.infolektuell.gradle.typst.tasks.MergePDFTask
import de.infolektuell.gradle.typst.tasks.TypstCompileTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin

class GradleTypstPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val store = TypstDataStore()
        val extension = project.extensions.create(TypstExtension.EXTENSION_NAME, TypstExtension::class.java)
        extension.root.convention(project.layout.projectDirectory.dir("src"))
        extension.dest.convention(project.layout.buildDirectory.dir("typst"))
        extension.ignoreSystemFonts.convention(true)
        extension.groups.configureEach { group ->
            group.root.convention((extension.root.dir(group.name)))
            group.ppi.convention(144)
            group.ignoreSystemFonts.convention(extension.ignoreSystemFonts)
            group.pdfStandard.convention(extension.pdfStandard)
            group.documents.configureEach { document ->
                document.ignoreSystemFonts.convention(group.ignoreSystemFonts)
                document.input.convention(group.root.file("typst/${document.name}.typ"))
                document.inputs.set(group.inputs)
                document.pdfStandard.convention(group.pdfStandard)
                document.pdf.configureEach {
                    it.outFile.convention(extension.dest.file("${group.name}/${document.name}/${it.name}/${document.name}.${it.extension}"))
                    it.pdfStandard.convention(PdfStandard.PDF_1_7)
                }
                document.png.configureEach {
                    it.outDir.convention(extension.dest.dir("${group.name}/${document.name}/${it.name}"))
                    it.filenameTemplate.convention("{0p}.${it.extension}")
                    it.ppi.convention(group.ppi)
                }
                document.svg.configureEach {
                    it.outDir.convention(extension.dest.dir("${group.name}/${document.name}/${it.name}"))
                    it.filenameTemplate.convention("{0p}.${it.extension}")
                }
            }
        }
        extension.merge.configureEach {
            it.target.convention(project.layout.buildDirectory.file("merged/${it.name}.pdf"))
        }
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
            dir.asFileTree.matching { spec -> spec.include("**/typst") }.singleFile
        }
        extension.executable.convention(project.layout.file(executableProvider))
        if (store.hasPackages) extension.localPackages.convention(project.layout.projectDirectory.dir(store.packageDir.toString()))
        project.tasks.withType(TypstCompileTask::class.java) { task ->
            task.executable.convention(extension.executable)
            task.root.convention(extension.root.asFile.map { it.absolutePath })
            task.packagePath.set(extension.localPackages)
            task.packageCachePath.set(store.packageCacheDir.toString())
            task.creationTimestamp.convention(extension.creationTimestamp)
        }
        extension.groups.all { group ->
            val taskName = "typstCompileGroup${group.name}"
            project.tasks.register(taskName, TypstCompileTask::class.java) { task ->
                task.includes.from(group.root, group.includes)
                task.fontDirectories.add(group.root.dir("fonts"))
                task.fontDirectories.addAll(group.includes.map { it.map { dir -> dir.dir("fonts") } })
                group.documents.all { document ->
                    document.pdf.all { target ->
                        target.compileTaskName.convention(taskName).finalizeValue()
                        project.objects.newInstance(TypstCompileTask.PdfDocumentConfig::class.java).apply {
                            input.set(document.input)
                            inputs.set(document.inputs)
                            ignoreSystemFonts.set(document.ignoreSystemFonts)
                            outFile.set(target.outFile)
                            pdfStandard.set(target.pdfStandard.map { it.value })
                        }.also { task.documents.add(it) }
                    }
                    document.png.all { target ->
                        target.compileTaskName.convention(taskName).finalizeValue()
                        project.objects.newInstance(TypstCompileTask.PngDocumentConfig::class.java).apply {
                            input.set(document.input)
                            inputs.set(document.inputs)
                            ignoreSystemFonts.set(document.ignoreSystemFonts)
                            outDir.set(target.outDir)
                            filenameTemplate.set(target.filenameTemplate)
                            ppi.set(target.ppi)
                        }.also { task.documents.add(it) }
                    }
                    document.png.all { target ->
                        target.compileTaskName.convention(taskName).finalizeValue()
                        project.objects.newInstance(TypstCompileTask.SvgDocumentConfig::class.java).apply {
                            input.set(document.input)
                            inputs.set(document.inputs)
                            ignoreSystemFonts.set(document.ignoreSystemFonts)
                            outDir.set(target.outDir)
                            filenameTemplate.set(target.filenameTemplate)
                        }.also { task.documents.add(it) }
                    }
                }
            }
        }
        extension.merge.all { merge ->
            project.tasks.register("merge${merge.name}", MergePDFTask::class.java) { task ->
                merge.source.all { target ->
                    task.documents.add(target.outFile)
                    task.dependsOn(target.compileTaskName)
                }
                task.merged.set(merge.target)
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
