package de.infolektuell.gradle.typst.tasks

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.process.ExecOperations
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import javax.inject.Inject

interface TypstParams : WorkParameters {
  val input: RegularFileProperty
  val output: RegularFileProperty
  val root: Property<String>
  val inputs: MapProperty<String, String>
  val fontPath: DirectoryProperty
  val compiler: Property<String>
}

abstract class TypstAction @Inject constructor(private val execOperations: ExecOperations) :
  WorkAction<TypstParams> {
  override fun execute() {
    execOperations.exec { action ->
      action.executable(parameters.compiler.get())
      action.args("compile")
      action.args("--root", parameters.root.get())
      if (parameters.fontPath.isPresent) action.args("--font-path", parameters.fontPath.asFile.get().absolutePath)
      parameters.inputs.get().forEach { (k, v) -> action.args("--input", "$k=$v") }
      action.args(parameters.input.asFile.get().absolutePath)
      action.args(parameters.output.asFile.get().absolutePath)
    }
  }
}
