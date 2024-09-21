package de.infolektuell.gradle.typst.tasks

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.process.ExecOperations
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import javax.inject.Inject

interface MagickParams : WorkParameters {
  val source: RegularFileProperty
  val target: RegularFileProperty
  val format: Property<String>
  val quality: Property<Int>
}

abstract class MagickAction @Inject constructor(private val execOperations: ExecOperations) :
  WorkAction<MagickParams> {
  override fun execute() {
    execOperations.exec { action ->
      action.commandLine("magick")
      action.args(parameters.source.asFile.get().absolutePath)
      action.args("-format", parameters.format.get())
      action.args("-quality", parameters.quality.get())
      action.args(parameters.target.asFile.get().absolutePath)
    }
  }
}
